# 🚀 Hướng Dẫn Deploy CNPM-Food trên Kubernetes

## 📋 Tóm tắt các cải tiến

Workflow CI/CD đã được tối ưu để giảm thời gian deploy từ **~15-20 phút** xuống **~3-5 phút**.

### Các vấn đề đã khắc phục

#### 1️⃣ **MySQL Timeout & Restart Loops**
- **Vấn đề cũ**: MySQL probe timeout 3-5s → container bị kill → restart loop
- **Giải pháp**: 
  - Tăng memory limit: `512Mi → 1Gi`
  - Thêm **startupProbe** (300s = 30 lần × 10s)
  - Extend liveness timeout: `5s → 10s`
  - Readiness probe thực sự kiểm tra DB query (`SELECT 1`)

#### 2️⃣ **Spring Boot Services Crash Before MySQL Ready**
- **Vấn đề cũ**: Services start đồng thời → kết nối MySQL fail → crash → restart
- **Giải pháp**:
  - Thêm **initContainer** đợi MySQL port 3306 mở
  - Services chỉ start sau khi MySQL đã sẵn sàng
  - JVM tuning: `-Xms256m -Xmx384m -XX:+UseG1GC`

#### 3️⃣ **Probe Timing Không Phù Hợp**
- **Vấn đề cũ**: Spring Boot cần 60-90s init nhưng liveness probe kill sau 60s
- **Giải pháp**:
  - **startupProbe**: Cho phép 200s khởi động (20 failures × 10s)
  - **livenessProbe**: Chỉ chạy sau khi startup hoàn tất
  - **readinessProbe**: Check `/actuator/health/readiness` thay vì `/actuator/health`

#### 4️⃣ **API Gateway Dependency**
- **Vấn đề cũ**: Gateway start trước backend services → fail routing
- **Giải pháp**: InitContainer đợi user-service & restaurant-service ready

---

## 🏗️ Kiến trúc Deployment Mới

```
┌─────────────┐
│   MySQL     │ ← Startup probe 300s, Memory 1Gi
└──────┬──────┘
       │ Port 3306
       ↓
┌──────────────────────────────────────┐
│  InitContainers (wait-for-mysql)     │ ← Đợi MySQL ready
└──────────────────────────────────────┘
       ↓
┌─────────────┬──────────────┬─────────────┬──────────────┐
│ User        │ Restaurant   │ Order       │ Payment      │
│ Service     │ Service      │ Service     │ Service      │
│ :8081       │ :8082        │ :8083       │ :8085        │
└──────┬──────┴──────┬───────┴──────┬──────┴──────┬───────┘
       │             │              │             │
       └─────────────┴──────────────┴─────────────┘
                     ↓
              ┌──────────────┐
              │ API Gateway  │ ← InitContainer wait backends
              │    :9000     │
              └──────┬───────┘
                     ↓
              ┌──────────────┐
              │  Frontend    │
              │    :4200     │
              └──────────────┘
```

---

## ⚡ Cải thiện Tốc độ Deploy

### Trước khi tối ưu:
```
Time   Event
0s     Deploy MySQL
30s    MySQL probe fail → restart
60s    MySQL probe fail → restart #2
90s    MySQL ready
90s    Services start → connect fail (DB init chưa chạy)
120s   Services liveness fail → restart
150s   Services restart #2
...    Restart loops tiếp tục 10-15 phút
```

### Sau khi tối ưu:
```
Time   Event
0s     Deploy MySQL (startup probe 300s window)
45s    MySQL ready (DB init script chạy)
45s    InitContainers start → wait MySQL port
50s    InitContainers complete
50s    Services start (JVM tuned)
90s    Services ready (startup probe 200s window)
95s    API Gateway start
120s   Gateway ready
125s   Frontend ready
→ Tổng: ~2-3 phút
```

---

## 📦 Thay đổi chính trong Manifests

### MySQL (`mysql-deployment.yaml`)
```yaml
resources:
  limits:
    memory: "1Gi"     # ↑ từ 512Mi
    cpu: "500m"       # ↑ từ 250m
startupProbe:         # ← MỚI
  failureThreshold: 30
  periodSeconds: 10
livenessProbe:
  timeoutSeconds: 10  # ↑ từ 5s
readinessProbe:
  command:
    - sh
    - -c
    - mysqladmin ping && mysql -e "SELECT 1"  # ← Check DB query
```

### Spring Boot Services
```yaml
spec:
  initContainers:     # ← MỚI
  - name: wait-for-mysql
    image: busybox:1.36
    command: ['sh', '-c', 'until nc -z mysql 3306; do sleep 2; done']
  
  containers:
  - env:
    - name: JAVA_OPTS  # ← MỚI - JVM tuning
      value: "-Xms256m -Xmx384m -XX:+UseG1GC"
    
    startupProbe:      # ← MỚI - 200s startup window
      failureThreshold: 20
      periodSeconds: 10
    
    livenessProbe:     # Simplified - chỉ check sau startup
      periodSeconds: 20
    
    readinessProbe:    # Better endpoint
      path: /actuator/health/readiness
```

---

## 🔧 Cách Deploy

### 1. Local Testing (Docker Desktop K8s)

```powershell
# Reset clean environment
kubectl delete namespace cnpm-food
kubectl create namespace cnpm-food

# Create MySQL init ConfigMap
kubectl create configmap mysql-init-script -n cnpm-food `
  --from-file=init.sql=mysql-init/init.sql

# Deploy MySQL first
kubectl apply -f k8s/services/mysql-deployment.yaml

# Wait for MySQL ready (~60s)
kubectl wait --for=condition=ready pod -l app=mysql -n cnpm-food --timeout=300s

# Deploy backend services
kubectl apply -f k8s/services/user-service-deployment.yaml
kubectl apply -f k8s/services/restaurant-service-deployment.yaml
kubectl apply -f k8s/services/order-service-deployment.yaml
kubectl apply -f k8s/services/payment-service-deployment.yaml

# Deploy gateway & frontend
kubectl apply -f k8s/services/api-gateway-deployment.yaml
kubectl apply -f k8s/services/frontend-deployment.yaml

# Monitor rollout
kubectl get pods -n cnpm-food -w
```

### 2. GitHub Actions (Automated)

Workflow tự động chạy khi push lên `main` hoặc `src`:

```yaml
Steps:
1. Build & Push Docker images (tag latest + SHA)
2. Set up kubectl với KUBE_CONFIG_DATA secret
3. Deploy MySQL với init script
4. Deploy microservices (parallel)
5. Wait for rollouts (7m timeout)
6. Upload diagnostics artifacts
7. Generate deployment summary
```

**Required Secrets:**
- `DOCKER_USERNAME`: Docker Hub username
- `DOCKER_TOKEN`: Docker Hub access token
- `KUBE_CONFIG_DATA`: base64-encoded kubeconfig

Tạo `KUBE_CONFIG_DATA`:
```powershell
# Local (Docker Desktop)
$kubeconfig = Get-Content ~/.kube/config -Raw
$bytes = [System.Text.Encoding]::UTF8.GetBytes($kubeconfig)
[Convert]::ToBase64String($bytes) | Set-Clipboard
# Paste vào GitHub Secrets
```

---

## 🐛 Troubleshooting

### Pods restart liên tục?
```powershell
# 1. Check logs
kubectl logs -l app=user-service -n cnpm-food --tail=100

# 2. Check events
kubectl get events -n cnpm-food --sort-by='.lastTimestamp' | Select-Object -Last 20

# 3. Describe pod
kubectl describe pod -l app=user-service -n cnpm-food

# 4. Verify MySQL
kubectl exec deploy/mysql -n cnpm-food -- mysql -uroot -prootpass -e "SHOW DATABASES;"
```

### MySQL init script không chạy?
```powershell
# PVC giữ dữ liệu cũ → phải xóa
kubectl delete pvc mysql-pvc -n cnpm-food
kubectl delete deployment mysql -n cnpm-food
kubectl apply -f k8s/services/mysql-deployment.yaml
```

### Service connect fail?
```powershell
# Verify DNS resolution
kubectl run -it --rm debug --image=busybox -n cnpm-food -- nslookup mysql

# Test connectivity
kubectl run -it --rm debug --image=busybox -n cnpm-food -- nc -zv mysql 3306
```

### Image pull slow?
```powershell
# Pre-pull images on all nodes
$images = @(
  "onlykohi/cnpm-user-service:latest",
  "onlykohi/cnpm-restaurant-service:latest",
  "onlykohi/cnpm-order-service:latest",
  "onlykohi/cnpm-payment-service:latest",
  "onlykohi/cnpm-api-gateway:latest",
  "onlykohi/cnpm-frontend:latest"
)

foreach ($img in $images) {
  docker pull $img
}
```

---

## 📊 Monitoring

### Check Service Health
```powershell
# All pods status
kubectl get pods -n cnpm-food

# Service endpoints
kubectl get svc -n cnpm-food

# Access health endpoints
kubectl port-forward svc/api-gateway 9000:9000 -n cnpm-food
# Open: http://localhost:9000/actuator/health

kubectl port-forward svc/frontend 4200:4200 -n cnpm-food
# Open: http://localhost:4200
```

### Logs Collection
```powershell
# Export all logs
$services = @("user-service", "restaurant-service", "order-service", "payment-service", "api-gateway", "frontend", "mysql")

foreach ($svc in $services) {
  kubectl logs -l app=$svc -n cnpm-food --tail=500 > "${svc}-logs.txt"
}
```

### Resource Usage (requires metrics-server)
```powershell
kubectl top nodes
kubectl top pods -n cnpm-food
```

---

## 🎯 Best Practices

### 1. **Staging Environment**
Workflow set `environment: staging` → có thể thêm manual approval gate:
```yaml
environment:
  name: staging
  # Cần approval từ team lead trước khi deploy
```

### 2. **Rollback Strategy**
```powershell
# View rollout history
kubectl rollout history deployment/user-service -n cnpm-food

# Rollback to previous version
kubectl rollout undo deployment/user-service -n cnpm-food

# Rollback to specific revision
kubectl rollout undo deployment/user-service --to-revision=2 -n cnpm-food
```

### 3. **Health Check Endpoints**
Đảm bảo Spring Boot `application.yml` enable:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  health:
    readinessState:
      enabled: true
    livenessState:
      enabled: true
```

### 4. **Resource Limits**
Adjust theo nhu cầu thực tế:
```yaml
resources:
  requests:   # Minimum guaranteed
    memory: "256Mi"
    cpu: "100m"
  limits:     # Maximum allowed
    memory: "512Mi"
    cpu: "250m"
```

---

## 🚦 Next Steps

1. **Monitoring Stack**: Prometheus + Grafana đã có manifest tại `k8s/monitoring/`
2. **Ingress**: Expose services via domain thay vì LoadBalancer
3. **Secrets Management**: Dùng Kubernetes Secrets thay vì hardcode password
4. **CI/CD Split**: Tách build (CI) và deploy (CD) workflows
5. **Auto-scaling**: HPA based on CPU/Memory

---

## 📞 Support

- **GitHub Issues**: Tạo issue mô tả vấn đề + attach logs
- **Artifacts**: Mỗi Actions run có `k8s-diagnostics` artifact download về
- **Summary**: Check Actions run → "Deployment Summary" section

---

**Updated**: 2025-11-15  
**Version**: 2.0 - Optimized for fast deployment
