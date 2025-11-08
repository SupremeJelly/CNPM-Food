# 🎉 HOÀN THÀNH - Kubernetes Deployment Package

## ✅ Đã tạo xong tất cả file cần thiết!

### 📁 Structure đã tạo:
```
d:\CNPM-Food\
├── k8s/
│   ├── README.md                         # Hướng dẫn chi tiết (đọc đầu tiên!)
│   ├── QUICKSTART.md                     # Hướng dẫn nhanh (recommended)
│   ├── DEPLOYMENT_SUMMARY.md             # Tóm tắt deployment
│   ├── ARCHITECTURE.md                   # Sơ đồ kiến trúc
│   ├── deploy.sh                         # Script tự động (Linux/Mac)
│   ├── deploy.ps1                        # Script tự động (Windows)
│   ├── hpa.yaml                          # Auto-scaling config
│   ├── ingress.yaml                      # Ingress config
│   ├── services/                         # Kubernetes deployments
│   │   ├── mysql-deployment.yaml
│   │   ├── user-service-deployment.yaml
│   │   ├── restaurant-service-deployment.yaml
│   │   ├── order-service-deployment.yaml
│   │   ├── payment-service-deployment.yaml
│   │   ├── api-gateway-deployment.yaml
│   │   └── frontend-deployment.yaml
│   └── monitoring/
│       └── servicemonitor.yaml           # Prometheus config
└── .github/workflows/
    └── deploy-k8s.yml                    # CI/CD pipeline
```

---

## 🚀 Câu hỏi của bạn: "Có phải up lên Docker Hub không?"

### Trả lời: **CÓ 3 OPTIONS** (chọn 1 trong 3)

### ✅ Option 1: Docker Hub (MIỄN PHÍ - Recommended cho học tập)
**Ưu điểm:**
- 🆓 Hoàn toàn miễn phí
- 📦 Không cần tài khoản Azure
- 🌍 Có thể deploy lên bất kỳ Kubernetes nào (AWS, GCP, Azure, hoặc local)

**Nhược điểm:**
- 🔓 Images public (ai cũng xem được)
- 🐌 Tải image chậm hơn

**Cách làm:**
```powershell
# 1. Đăng ký Docker Hub (nếu chưa có): https://hub.docker.com/

# 2. Login
docker login

# 3. Build và push images (thay "yourusername" bằng username Docker Hub của bạn)
docker build -t yourusername/cnpm-user-service:latest ./user-service
docker push yourusername/cnpm-user-service:latest

docker build -t yourusername/cnpm-restaurant-service:latest ./restaurant-service
docker push yourusername/cnpm-restaurant-service:latest

docker build -t yourusername/cnpm-order-service:latest ./order-service
docker push yourusername/cnpm-order-service:latest

docker build -t yourusername/cnpm-payment-service:latest ./payment-service
docker push yourusername/cnpm-payment-service:latest

docker build -t yourusername/cnpm-api-gateway:latest ./api-gateway
docker push yourusername/cnpm-api-gateway:latest

docker build -t yourusername/cnpm-frontend:latest ./frontend
docker push yourusername/cnpm-frontend:latest
```

---

### ✅ Option 2: Azure Container Registry (Recommended cho Production)
**Ưu điểm:**
- 🔒 Private registry (bảo mật)
- ⚡ Rất nhanh (cùng region với AKS)
- 🏢 Professional

**Nhược điểm:**
- 💰 Tốn tiền (~$5/tháng + $60/tháng cho AKS)
- 🎓 Cần học Azure

**Cách làm:**
```powershell
# 1. Login Azure
az login

# 2. Tạo Resource Group
az group create --name cnpm-food-rg --location southeastasia

# 3. Tạo Container Registry
az acr create --resource-group cnpm-food-rg --name cnpmfoodacr --sku Basic

# 4. Login vào ACR
az acr login --name cnpmfoodacr

# 5. Build và push
docker build -t cnpmfoodacr.azurecr.io/user-service:latest ./user-service
docker push cnpmfoodacr.azurecr.io/user-service:latest
# Repeat for all services...
```

---

### ✅ Option 3: Local Kubernetes (Test trước khi deploy cloud)
**Ưu điểm:**
- 🆓 Hoàn toàn miễn phí
- 💻 Test ngay trên máy
- 🔧 Không cần push image lên đâu cả

**Cách làm:**
```powershell
# 1. Enable Kubernetes trong Docker Desktop
# Settings → Kubernetes → Enable Kubernetes

# 2. Build images (không cần push)
docker-compose build

# 3. Deploy local
kubectl apply -f k8s/services/
```

---

## 🎯 Khuyến nghị của tôi:

### 🥇 Cho học tập / demo:
**→ Dùng Option 1 (Docker Hub)**
- Miễn phí 100%
- Đơn giản nhất
- Deploy được lên mọi cloud

### 🥈 Cho dự án thực tế:
**→ Dùng Option 2 (Azure ACR + AKS)**
- Bảo mật tốt
- Performance tốt
- Có thể dùng Azure Free Trial ($200 credit)

### 🥉 Cho test local:
**→ Dùng Option 3 (Docker Desktop K8s)**
- Test trước khi deploy
- Debug dễ dàng
- Không tốn tiền

---

## 📖 Hướng dẫn Deploy từng bước (Docker Hub)

### Bước 1: Push images lên Docker Hub
```powershell
cd d:\CNPM-Food

# Thay "yourusername" bằng username Docker Hub của bạn
$USERNAME = "yourusername"

# Build tất cả
docker-compose build

# Tag và push
docker tag cnpm-food-user-service $USERNAME/cnpm-user-service:latest
docker push $USERNAME/cnpm-user-service:latest

docker tag cnpm-food-restaurant-service $USERNAME/cnpm-restaurant-service:latest
docker push $USERNAME/cnpm-restaurant-service:latest

docker tag cnpm-food-order-service $USERNAME/cnpm-order-service:latest
docker push $USERNAME/cnpm-order-service:latest

docker tag cnpm-food-payment-service $USERNAME/cnpm-payment-service:latest
docker push $USERNAME/cnpm-payment-service:latest

docker tag cnpm-food-api-gateway $USERNAME/cnpm-api-gateway:latest
docker push $USERNAME/cnpm-api-gateway:latest

docker tag cnpm-food-frontend $USERNAME/cnpm-frontend:latest
docker push $USERNAME/cnpm-frontend:latest
```

### Bước 2: Update image names trong K8s files
Mở từng file trong `k8s/services/` và thay:
```yaml
image: cnpmfoodacr.azurecr.io/user-service:latest
```
thành:
```yaml
image: yourusername/cnpm-user-service:latest
```

**Hoặc dùng PowerShell để tự động:**
```powershell
$USERNAME = "yourusername"
Get-ChildItem k8s/services/*.yaml | ForEach-Object {
    (Get-Content $_) -replace 'cnpmfoodacr.azurecr.io/', "$USERNAME/" | Set-Content $_
}
```

### Bước 3: Deploy lên Kubernetes
```powershell
# Tạo namespace
kubectl create namespace cnpm-food

# Deploy MySQL
kubectl apply -f k8s/services/mysql-deployment.yaml

# Đợi MySQL ready (3-5 phút)
kubectl wait --for=condition=ready pod -l app=mysql -n cnpm-food --timeout=300s

# Deploy tất cả services
kubectl apply -f k8s/services/user-service-deployment.yaml
kubectl apply -f k8s/services/restaurant-service-deployment.yaml
kubectl apply -f k8s/services/order-service-deployment.yaml
kubectl apply -f k8s/services/payment-service-deployment.yaml
kubectl apply -f k8s/services/api-gateway-deployment.yaml
kubectl apply -f k8s/services/frontend-deployment.yaml

# Xem status
kubectl get pods -n cnpm-food
```

### Bước 4: Deploy Monitoring
```powershell
# Add Helm repo
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install Prometheus + Grafana
helm install monitoring prometheus-community/kube-prometheus-stack `
  --namespace monitoring `
  --create-namespace `
  --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false `
  --set grafana.adminPassword=admin

# Apply ServiceMonitors
kubectl apply -f k8s/monitoring/servicemonitor.yaml
```

### Bước 5: Access ứng dụng
```powershell
# Frontend
kubectl port-forward svc/frontend -n cnpm-food 4200:80
# Mở browser: http://localhost:4200

# Grafana
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80
# Mở browser: http://localhost:3000 (admin/admin)

# Prometheus
kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090:9090
# Mở browser: http://localhost:9090
```

---

## 🎓 Video hướng dẫn (tự tìm):
- Kubernetes Tutorial for Beginners
- Deploy Spring Boot to Kubernetes
- Prometheus & Grafana on Kubernetes
- Azure AKS Tutorial

---

## 📚 Đọc thêm:
1. **k8s/QUICKSTART.md** - Hướng dẫn nhanh nhất
2. **k8s/README.md** - Hướng dẫn chi tiết
3. **k8s/ARCHITECTURE.md** - Hiểu kiến trúc
4. **k8s/DEPLOYMENT_SUMMARY.md** - Tổng quan

---

## ❓ FAQ

**Q: Tôi nên chọn option nào?**
A: Nếu học thì dùng Docker Hub (free). Nếu làm dự án thật thì dùng Azure.

**Q: Docker Hub có giới hạn gì không?**
A: Free tier có 1 private repo, unlimited public repos. Pull rate limit 200 requests/6h.

**Q: Tốn bao nhiêu tiền nếu dùng Azure?**
A: ~$90/tháng cho setup đầy đủ. Có thể giảm xuống ~$35/tháng nếu dùng 1 node.

**Q: Có thể dùng AWS hoặc GCP không?**
A: Được! Kubernetes manifests giống nhau. Chỉ cần đổi registry và load balancer.

**Q: Monitoring có chạy không?**
A: Có! Prometheus + Grafana tự động thu thập metrics từ tất cả services.

**Q: Có cần setup thêm gì không?**
A: Không! Chỉ cần push images và deploy. Monitoring tự động hoạt động.

---

## 🆘 Gặp lỗi?

### ImagePullBackOff:
```powershell
# Kiểm tra image có tồn tại không
docker pull yourusername/cnpm-user-service:latest

# Nếu private repo, tạo secret
kubectl create secret docker-registry regcred `
  --docker-server=https://index.docker.io/v1/ `
  --docker-username=yourusername `
  --docker-password=yourpassword `
  -n cnpm-food
```

### CrashLoopBackOff:
```powershell
# Xem logs
kubectl logs <pod-name> -n cnpm-food

# Xem chi tiết
kubectl describe pod <pod-name> -n cnpm-food
```

---

## ✅ Checklist Deploy

- [ ] Đã chọn registry (Docker Hub hoặc Azure ACR)
- [ ] Đã build tất cả Docker images
- [ ] Đã push images lên registry
- [ ] Đã update image names trong K8s files
- [ ] Đã có Kubernetes cluster (local hoặc cloud)
- [ ] Đã deploy MySQL và đợi ready
- [ ] Đã deploy tất cả microservices
- [ ] Đã deploy monitoring stack
- [ ] Đã test frontend accessible
- [ ] Đã test Grafana dashboards

---

## 🎉 Kết luận

Tôi đã tạo **COMPLETE Kubernetes deployment package** cho bạn với:

✅ **8 Kubernetes deployment files** (MySQL + 6 services)
✅ **ServiceMonitor** cho Prometheus auto-discovery
✅ **HPA** cho auto-scaling
✅ **Ingress** cho production
✅ **CI/CD pipeline** với GitHub Actions
✅ **Deployment scripts** (Windows + Linux)
✅ **4 documentation files** với hướng dẫn chi tiết
✅ **Architecture diagram**

**TẤT CẢ đã sẵn sàng để deploy!** 🚀

Monitoring stack (Prometheus + Grafana) sẽ **TỰ ĐỘNG HOẠT ĐỘNG** khi bạn deploy, không cần config thêm gì!

---

**Next Step**: Đọc file `k8s/QUICKSTART.md` và làm theo! 😊
