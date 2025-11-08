# 🚀 Deploy Monitoring Stack (Không dùng Helm)

## Tổng quan
Monitoring stack bao gồm:
- **Prometheus** - Thu thập metrics từ các services
- **Grafana** - Visualization và dashboards

## 📋 Bước 1: Deploy Prometheus

```powershell
# Deploy Prometheus với ServiceAccount, ClusterRole và Service
kubectl apply -f k8s/monitoring/prometheus-deployment.yaml
```

**Kiểm tra:**
```powershell
kubectl get pods -n monitoring
kubectl get svc -n monitoring
```

## 📋 Bước 2: Deploy Grafana

```powershell
# Deploy Grafana với datasource và dashboard sẵn
kubectl apply -f k8s/monitoring/grafana-deployment.yaml
```

**Kiểm tra:**
```powershell
kubectl get pods -n monitoring
# Chờ đến khi cả 2 pods đều Running và Ready (1/1)
```

## 🌐 Bước 3: Truy cập Monitoring

### Prometheus (Port 30090)
```powershell
# Truy cập qua NodePort
# Mở browser: http://localhost:30090

# HOẶC port-forward (nếu NodePort không hoạt động)
kubectl port-forward svc/prometheus -n monitoring 9090:9090
# Mở browser: http://localhost:9090
```

**Test Prometheus:**
1. Mở **http://localhost:30090** (hoặc :9090)
2. Vào **Status → Targets** - Xem các services đang được scrape
3. Vào **Graph** và query: `up{namespace="cnpm-food"}` - Xem services nào đang up

### Grafana (Port 30300)
```powershell
# Truy cập qua NodePort
# Mở browser: http://localhost:30300

# HOẶC port-forward
kubectl port-forward svc/grafana -n monitoring 3000:3000
# Mở browser: http://localhost:3000
```

**Login Grafana:**
- Username: `admin`
- Password: `admin`

**Xem Dashboard:**
1. Login vào Grafana
2. Vào **Dashboards** → Xem dashboard "CNPM Food Services"
3. Dashboard sẽ hiển thị:
   - HTTP Request Rate của từng service
   - CPU Usage
   - Memory Usage
   - JVM metrics

## 📊 Bước 4: Verify Metrics

### Kiểm tra Prometheus đang scrape metrics:
```powershell
# Vào Prometheus UI (localhost:30090)
# Chạy các queries sau:

# 1. Xem tất cả services đang up
up{namespace="cnpm-food"}

# 2. HTTP request rate
rate(http_server_requests_seconds_count{namespace="cnpm-food"}[5m])

# 3. CPU usage
process_cpu_usage{namespace="cnpm-food"}

# 4. Memory usage
jvm_memory_used_bytes{namespace="cnpm-food"}

# 5. Database connections
hikaricp_connections_active{namespace="cnpm-food"}
```

## 🔧 Troubleshooting

### Prometheus không scrape được services?
```powershell
# 1. Kiểm tra ServiceAccount có quyền không
kubectl get clusterrolebinding prometheus -o yaml

# 2. Kiểm tra pods có annotation không
kubectl get pods -n cnpm-food -o jsonpath='{.items[*].metadata.annotations}'

# 3. Xem Prometheus logs
kubectl logs -f deployment/prometheus -n monitoring

# 4. Kiểm tra Prometheus config
kubectl exec -it deployment/prometheus -n monitoring -- cat /etc/prometheus/prometheus.yml
```

### Grafana không kết nối được Prometheus?
```powershell
# 1. Test connection từ Grafana pod
kubectl exec -it deployment/grafana -n monitoring -- curl http://prometheus:9090/api/v1/targets

# 2. Kiểm tra Grafana logs
kubectl logs -f deployment/grafana -n monitoring

# 3. Verify datasource config
kubectl get configmap grafana-datasources -n monitoring -o yaml
```

### Services không expose metrics?
```powershell
# 1. Kiểm tra actuator endpoint
kubectl exec -it deployment/user-service -n cnpm-food -- curl http://localhost:8081/actuator/prometheus

# 2. Kiểm tra pod annotations
kubectl describe pod -l app=user-service -n cnpm-food | grep prometheus.io

# 3. Verify port trong annotation
# Nên thấy:
# prometheus.io/scrape: "true"
# prometheus.io/path: "/actuator/prometheus"
# prometheus.io/port: "8081"
```

## 📈 Metrics có sẵn

### JVM Metrics:
- `jvm_memory_used_bytes` - Memory usage
- `jvm_memory_max_bytes` - Max memory
- `jvm_gc_pause_seconds_count` - GC count
- `jvm_threads_live_threads` - Thread count

### HTTP Metrics:
- `http_server_requests_seconds_count` - Request count
- `http_server_requests_seconds_sum` - Request duration
- `http_server_requests_seconds_max` - Max response time

### Database Metrics:
- `hikaricp_connections_active` - Active DB connections
- `hikaricp_connections_idle` - Idle connections
- `hikaricp_connections_pending` - Pending connections

### System Metrics:
- `process_cpu_usage` - CPU usage
- `system_cpu_usage` - System CPU
- `process_uptime_seconds` - Uptime

## 🎯 Tạo Custom Dashboards trong Grafana

1. **Login Grafana** (localhost:30300, admin/admin)
2. **Create → Dashboard**
3. **Add Panel**
4. **Chọn Prometheus datasource**
5. **Query metrics** (ví dụ: `rate(http_server_requests_seconds_count[5m])`)
6. **Customize visualization**
7. **Save dashboard**

## 🗑️ Xóa Monitoring Stack

```powershell
# Xóa tất cả
kubectl delete -f k8s/monitoring/grafana-deployment.yaml
kubectl delete -f k8s/monitoring/prometheus-deployment.yaml

# Hoặc xóa namespace (sẽ xóa tất cả)
kubectl delete namespace monitoring
```

## 📝 Ghi chú

- **NodePort 30090** - Prometheus (chỉ dùng local/test)
- **NodePort 30300** - Grafana (chỉ dùng local/test)
- Với production, nên dùng Ingress thay vì NodePort
- Data của Prometheus/Grafana lưu trong emptyDir (mất khi pod restart)
- Để persistent data, cần thêm PersistentVolumeClaim

## ✅ Quick Check

```powershell
# Tất cả nên Running và Ready 1/1
kubectl get pods -n monitoring

# Output mong đợi:
# NAME                          READY   STATUS    RESTARTS   AGE
# prometheus-xxxxx              1/1     Running   0          2m
# grafana-xxxxx                 1/1     Running   0          1m
```

```powershell
# Services nên có NodePort
kubectl get svc -n monitoring

# Output mong đợi:
# NAME         TYPE       CLUSTER-IP     EXTERNAL-IP   PORT(S)          AGE
# prometheus   NodePort   10.x.x.x       <none>        9090:30090/TCP   2m
# grafana      NodePort   10.x.x.x       <none>        3000:30300/TCP   1m
```

---

**🎉 Done!** Monitoring stack đã sẵn sàng! Truy cập:
- Prometheus: http://localhost:30090
- Grafana: http://localhost:30300 (admin/admin)
