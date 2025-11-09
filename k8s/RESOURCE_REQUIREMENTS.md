# 📊 Resource Requirements - CNPM Food Application

## 🎯 Minimum System Requirements

Để chạy application này trên **Docker Desktop Kubernetes**, máy của bạn cần:

### **Hardware Requirements**
- **RAM**: Tối thiểu **4GB**, khuyến nghị **8GB**
- **CPU**: Tối thiểu 2 cores, khuyến nghị 4 cores
- **Disk**: Tối thiểu 20GB free space

### **Docker Desktop Settings**
1. Mở Docker Desktop → Settings → Resources
2. Cấu hình:
   - **Memory**: 4GB (minimum) - 6GB (recommended)
   - **CPUs**: 2 (minimum) - 4 (recommended)
   - **Swap**: 1GB
   - **Disk image size**: 60GB

## 📦 Resource Allocation per Service

### **Tổng Resource Usage**

| Component | Request Memory | Limit Memory | Request CPU | Limit CPU |
|-----------|----------------|--------------|-------------|-----------|
| MySQL | 256Mi | 512Mi | 100m | 250m |
| User Service | 256Mi | 512Mi | 100m | 250m |
| Order Service | 256Mi | 512Mi | 100m | 250m |
| Restaurant Service | 256Mi | 512Mi | 100m | 250m |
| Payment Service | 256Mi | 512Mi | 100m | 250m |
| API Gateway | 256Mi | 512Mi | 100m | 250m |
| Frontend | 128Mi | 256Mi | 50m | 100m |
| **TOTAL** | **~1.6GB** | **~3.3GB** | **750m** | **1.65 cores** |

### **With Monitoring Stack** (Optional)

| Component | Request Memory | Limit Memory | Request CPU | Limit CPU |
|-----------|----------------|--------------|-------------|-----------|
| Prometheus | 256Mi | 512Mi | 100m | 250m |
| Grafana | 128Mi | 256Mi | 50m | 100m |
| **TOTAL + Monitoring** | **~2GB** | **~4GB** | **900m** | **2 cores** |

## ⚙️ Resource Optimization Tips

### 1. **Nếu máy có RAM < 4GB**
Giảm số lượng replicas hoặc tắt một số services không cần thiết:

```bash
# Tắt monitoring
kubectl delete namespace monitoring

# Hoặc giảm resource limits
kubectl set resources deployment/user-service -n cnpm-food --limits=memory=384Mi
```

### 2. **Nếu máy chạy chậm**
- Tắt các ứng dụng khác khi chạy K8s
- Giảm số lượng replicas xuống 1 (đã làm)
- Tắt liveness/readiness probes trong lúc development

### 3. **Kiểm tra resource usage**

```bash
# Xem resource usage của pods
kubectl top pods -n cnpm-food

# Xem resource usage của nodes
kubectl top nodes

# Xem chi tiết resource của pod cụ thể
kubectl describe pod <pod-name> -n cnpm-food
```

## 🚨 Common Issues & Solutions

### **Issue 1: Pods bị "OOMKilled" (Out of Memory)**
```bash
# Kiểm tra logs
kubectl describe pod <pod-name> -n cnpm-food

# Giải pháp: Tăng Docker Desktop memory
# Docker Desktop → Settings → Resources → Memory: 6GB
```

### **Issue 2: Pods ở trạng thái "Pending"**
```bash
# Kiểm tra events
kubectl get events -n cnpm-food --sort-by='.lastTimestamp'

# Nguyên nhân thường gặp:
# - Không đủ CPU/Memory
# - PersistentVolumeClaim chưa bound

# Giải pháp: Tăng resource hoặc giảm requests
```

### **Issue 3: Frontend không load được**
```bash
# Kiểm tra service
kubectl get svc -n cnpm-food

# Đảm bảo frontend service type là LoadBalancer
# Port mapping: 80 → 4200
```

### **Issue 4: Services khởi động chậm**
- Spring Boot services cần ~60-90s để start
- MySQL cần ~30s để ready
- Đừng lo lắng nếu pods ở trạng thái "ContainerCreating" hoặc "Running" nhưng chưa Ready

## 📝 Deployment Sequence

Để tránh lỗi khi deploy, làm theo thứ tự:

```bash
# 1. Tạo namespace
kubectl create namespace cnpm-food

# 2. Deploy MySQL trước (database)
kubectl apply -f k8s/services/mysql-deployment.yaml
kubectl wait --for=condition=ready pod -l app=mysql -n cnpm-food --timeout=300s

# 3. Deploy backend services
kubectl apply -f k8s/services/user-service-deployment.yaml
kubectl apply -f k8s/services/restaurant-service-deployment.yaml
kubectl apply -f k8s/services/order-service-deployment.yaml
kubectl apply -f k8s/services/payment-service-deployment.yaml

# 4. Deploy API Gateway
kubectl apply -f k8s/services/api-gateway-deployment.yaml

# 5. Deploy Frontend
kubectl apply -f k8s/services/frontend-deployment.yaml

# 6. Kiểm tra
kubectl get pods -n cnpm-food
kubectl get svc -n cnpm-food
```

## 🔧 Testing & CI/CD

### **Local Testing Before Deploy**
```bash
# Test từng service
cd user-service && ./mvnw clean test
cd order-service && ./mvnw clean test
cd restaurant-service && ./mvnw clean test
cd payment-service && ./mvnw clean test
cd api-gateway && ./mvnw clean test

# Build Docker images locally
docker build -t onlykohi/cnpm-user-service:latest ./user-service
docker build -t onlykohi/cnpm-order-service:latest ./order-service
# ... (các services khác)
```

### **CI/CD Workflow**
Workflow đã được cập nhật để:
1. ✅ **Chạy tests trước** khi build images
2. ✅ **Build & push images** chỉ khi tests pass
3. ✅ **Deploy to K8s** theo đúng sequence
4. ✅ **Verify deployment** sau khi deploy xong

## 📊 Monitoring Resource Usage

```bash
# Enable metrics server (nếu chưa có)
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# Xem resource usage real-time
watch kubectl top pods -n cnpm-food

# Xem resource limits và requests
kubectl describe deployments -n cnpm-food | grep -A 5 "Limits\|Requests"
```

## 🎓 Best Practices

1. **Development**: Resource limits thấp để tiết kiệm tài nguyên
2. **Staging**: Resource limits trung bình để test performance
3. **Production**: Resource limits cao để đảm bảo stability

4. **Luôn set requests < limits** để K8s có thể schedule pods hiệu quả

5. **Monitor thường xuyên** để điều chỉnh resource phù hợp:
   ```bash
   kubectl top pods -n cnpm-food
   ```

## 📞 Support

Nếu gặp vấn đề về resource:
1. Kiểm tra Docker Desktop settings
2. Xem logs: `kubectl logs <pod-name> -n cnpm-food`
3. Xem events: `kubectl get events -n cnpm-food`
4. Describe pod: `kubectl describe pod <pod-name> -n cnpm-food`

---

**Last Updated**: November 9, 2025  
**Optimized for**: Docker Desktop Kubernetes on personal computers
