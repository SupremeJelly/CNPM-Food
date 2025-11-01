# 📊 Monitoring Setup - Prometheus + Grafana

## Overview
Hệ thống monitoring cho Food Delivery Application sử dụng:
- **Prometheus**: Thu thập và lưu trữ metrics
- **Grafana**: Visualization và dashboards
- **Spring Boot Actuator**: Expose metrics từ các microservices
- **Micrometer**: Bridge giữa Spring Boot và Prometheus

## 🚀 Quick Start

### 1. Build và start tất cả services
```powershell
# Build lại các services (vì đã thêm dependencies mới)
docker-compose build

# Start tất cả containers
docker-compose up -d
```

### 2. Truy cập Monitoring Tools

**Prometheus UI:**
- URL: http://localhost:9090
- Xem targets: http://localhost:9090/targets
- Query metrics trực tiếp

**Grafana UI:**
- URL: http://localhost:3000
- Username: `admin`
- Password: `admin`
- Đổi password lần đầu login (hoặc skip)

## 📈 Metrics Endpoints

Mỗi service expose metrics tại `/actuator/prometheus`:

| Service | URL |
|---------|-----|
| API Gateway | http://localhost:9000/actuator/prometheus |
| User Service | http://localhost:8081/actuator/prometheus |
| Restaurant Service | http://localhost:8082/actuator/prometheus |
| Order Service | http://localhost:8083/actuator/prometheus |
| Notification Service | http://localhost:8084/actuator/prometheus |
| Payment Service | http://localhost:8085/actuator/prometheus |

## 🎯 Kiểm tra Prometheus Targets

1. Mở http://localhost:9090/targets
2. Kiểm tra tất cả targets có status **UP** (màu xanh)
3. Nếu có target DOWN (màu đỏ), kiểm tra:
   - Service đó đã start chưa
   - Actuator endpoint có accessible không

## 📊 Tạo Dashboard trong Grafana

### Option 1: Import Dashboard có sẵn

1. Login vào Grafana
2. Click **+** → **Import**
3. Nhập Dashboard ID: **11378** (Spring Boot 2.1 Statistics)
4. Hoặc ID: **4701** (JVM Micrometer)
5. Select Prometheus data source
6. Click **Import**

### Option 2: Tạo Dashboard mới

1. Click **+** → **Dashboard** → **Add new panel**
2. Chọn metrics muốn visualize:
   - `http_server_requests_seconds_count` - Request count
   - `http_server_requests_seconds_sum` - Request duration
   - `jvm_memory_used_bytes` - JVM memory
   - `system_cpu_usage` - CPU usage
3. Customize visualization
4. Save dashboard

## 🔍 Useful Prometheus Queries

### HTTP Request Rate
```promql
rate(http_server_requests_seconds_count{application="order-service"}[1m])
```

### Request Duration (95th percentile)
```promql
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))
```

### JVM Memory Usage
```promql
jvm_memory_used_bytes{application="user-service", area="heap"}
```

### Error Rate
```promql
rate(http_server_requests_seconds_count{status=~"5.."}[1m])
```

### Active Database Connections
```promql
hikaricp_connections_active{application="restaurant-service"}
```

## 🎨 Recommended Dashboards

### 1. **Spring Boot 2.1 Statistics (ID: 11378)**
- Overview metrics cho Spring Boot apps
- JVM memory, threads, CPU
- HTTP requests & errors
- Database connections

### 2. **JVM (Micrometer) (ID: 4701)**
- Detailed JVM metrics
- Garbage collection
- Thread analysis
- Class loading

### 3. **Spring Boot Statistics (ID: 6756)**
- API performance
- Database metrics
- Cache statistics

## 🛠️ Troubleshooting

### Prometheus không thấy targets
```powershell
# Kiểm tra Prometheus logs
docker logs prometheus

# Verify prometheus.yml
docker exec prometheus cat /etc/prometheus/prometheus.yml
```

### Service metrics không accessible
```powershell
# Test actuator endpoint
curl http://localhost:8081/actuator/health
curl http://localhost:8081/actuator/prometheus

# Kiểm tra service logs
docker logs user-service
```

### Grafana không connect được Prometheus
```powershell
# Kiểm tra Grafana logs
docker logs grafana

# Test connectivity từ Grafana container
docker exec grafana curl http://prometheus:9090/api/v1/status/config
```

## 📁 File Structure

```
monitoring/
├── prometheus.yml              # Prometheus config
├── actuator-config.yml         # Actuator config template
└── grafana/
    └── provisioning/
        └── datasources/
            └── prometheus.yml  # Auto-provision Prometheus datasource
```

## 🔐 Security Notes

**Production checklist:**
- Thay đổi Grafana admin password
- Enable authentication cho Prometheus
- Restrict metrics endpoints (Spring Security)
- Use HTTPS
- Setup alerting rules

## 📚 Additional Resources

- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)

## 🎯 Next Steps

1. ✅ Import recommended dashboards
2. ✅ Setup alerting rules trong Prometheus
3. ✅ Configure email notifications trong Grafana
4. ✅ Create custom dashboards cho business metrics
5. ✅ Setup data retention policies

---

**Happy Monitoring! 📊🚀**
