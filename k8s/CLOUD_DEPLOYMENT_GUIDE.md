# ☁️ Cloud Deployment Guide

Hướng dẫn deploy ứng dụng CNPM Food lên các nền tảng cloud phổ biến.

## 📋 Prerequisites

Trước khi deploy, đảm bảo bạn đã:
- ✅ Test thành công trên Docker Desktop Kubernetes
- ✅ Tất cả images đã push lên Docker Hub registry `onlykohi/*`
- ✅ Đã có tài khoản cloud (Azure/GCP/AWS)

---

## 🔵 Option 1: Azure Kubernetes Service (AKS) - RECOMMENDED

### **Tại sao chọn Azure?**
- ✅ Tích hợp tốt với GitHub Actions (đã có workflow sẵn trong repo)
- ✅ Azure Container Registry (ACR) miễn phí tier Basic
- ✅ Free tier: $200 credit cho 30 ngày đầu
- ✅ Dễ setup monitoring với Azure Monitor

### **Bước 1: Cài Azure CLI**
```powershell
# Download và cài Azure CLI
winget install -e --id Microsoft.AzureCLI

# Hoặc dùng installer
# https://aka.ms/installazurecliwindows

# Verify installation
az version
```

### **Bước 2: Login và tạo resource**
```powershell
# Login vào Azure
az login

# Set subscription (nếu có nhiều subscription)
az account list --output table
az account set --subscription "YOUR_SUBSCRIPTION_ID"

# Tạo resource group
az group create --name cnpm-food-rg --location eastus

# Tạo AKS cluster (3 nodes, Standard_B2s - ~$60/tháng)
az aks create \
  --resource-group cnpm-food-rg \
  --name cnpm-food-aks \
  --node-count 3 \
  --node-vm-size Standard_B2s \
  --enable-addons monitoring \
  --generate-ssh-keys
```

### **Bước 3: Connect tới AKS cluster**
```powershell
# Get credentials
az aks get-credentials --resource-group cnpm-food-rg --name cnpm-food-aks

# Verify connection
kubectl cluster-info
kubectl get nodes
```

### **Bước 4: Update deployment files cho Azure**

**Sửa `k8s/services/mysql-deployment.yaml`:**
```yaml
# Uncomment dòng này để dùng Azure managed disk
storageClassName: managed-csi  # For Azure AKS
```

**Tạo Azure Container Registry (ACR):**
```powershell
# Tạo ACR
az acr create --resource-group cnpm-food-rg \
  --name cnpmfoodacr --sku Basic

# Login vào ACR
az acr login --name cnpmfoodacr

# Attach ACR to AKS
az aks update -n cnpm-food-aks -g cnpm-food-rg \
  --attach-acr cnpmfoodacr
```

**Update image registry trong deployments:**
```powershell
# Option A: Push images từ Docker Hub sang ACR
docker pull onlykohi/cnpm-user-service:latest
docker tag onlykohi/cnpm-user-service:latest cnpmfoodacr.azurecr.io/user-service:latest
docker push cnpmfoodacr.azurecr.io/user-service:latest

# Lặp lại cho các images khác:
# - onlykohi/cnpm-restaurant-service → cnpmfoodacr.azurecr.io/restaurant-service
# - onlykohi/cnpm-order-service → cnpmfoodacr.azurecr.io/order-service
# - onlykohi/cnpm-payment-service → cnpmfoodacr.azurecr.io/payment-service
# - onlykohi/cnpm-api-gateway → cnpmfoodacr.azurecr.io/api-gateway
# - onlykohi/cnpm-frontend → cnpmfoodacr.azurecr.io/frontend

# Option B: Giữ nguyên Docker Hub (đơn giản hơn)
# Không cần thay đổi gì, AKS sẽ pull từ Docker Hub
```

### **Bước 5: Deploy application**
```powershell
# Tạo namespace
kubectl create namespace cnpm-food

# Deploy MySQL trước
kubectl apply -f k8s/services/mysql-deployment.yaml

# Đợi MySQL ready
kubectl wait --for=condition=ready pod -l app=mysql -n cnpm-food --timeout=300s

# Deploy tất cả services
kubectl apply -f k8s/services/

# Kiểm tra deployment
kubectl get pods -n cnpm-food
kubectl get svc -n cnpm-food
```

### **Bước 6: Expose services ra internet**
```powershell
# Frontend và API Gateway đã có type: LoadBalancer
# Azure sẽ tự tạo public IP

# Xem public IP
kubectl get svc -n cnpm-food

# Hoặc dùng Ingress Controller (recommended cho production)
# Xem file k8s/ingress.yaml đã được tạo sẵn
```

### **Bước 7: Setup CI/CD với GitHub Actions**

Đã có sẵn workflow file trong `.github/workflows/deploy-aks.yml`. Cần tạo secrets:

```powershell
# Tạo Service Principal cho GitHub Actions
az ad sp create-for-rbac --name "github-actions-cnpm-food" \
  --role contributor \
  --scopes /subscriptions/{subscription-id}/resourceGroups/cnpm-food-rg \
  --sdk-auth

# Copy output JSON và tạo GitHub Secret tên AZURE_CREDENTIALS
```

**GitHub Secrets cần tạo:**
- `AZURE_CREDENTIALS` - JSON từ lệnh trên
- `REGISTRY_LOGIN_SERVER` - `cnpmfoodacr.azurecr.io`
- `REGISTRY_USERNAME` - ACR username
- `REGISTRY_PASSWORD` - ACR password

### **Chi phí ước tính Azure:**
- AKS cluster (3 x Standard_B2s): ~$60/tháng
- Azure Load Balancer: ~$20/tháng
- Managed Disk (30GB): ~$5/tháng
- **Tổng: ~$85/tháng**

---

## 🔴 Option 2: Google Kubernetes Engine (GKE)

### **Bước 1: Cài Google Cloud SDK**
```powershell
# Download từ: https://cloud.google.com/sdk/docs/install
# Sau khi cài xong:
gcloud init
gcloud auth login
```

### **Bước 2: Tạo GKE cluster**
```powershell
# Set project
gcloud config set project YOUR_PROJECT_ID

# Enable APIs
gcloud services enable container.googleapis.com

# Tạo cluster (3 nodes e2-medium)
gcloud container clusters create cnpm-food-gke \
  --zone us-central1-a \
  --num-nodes 3 \
  --machine-type e2-medium \
  --enable-autoscaling \
  --min-nodes 2 \
  --max-nodes 5

# Connect
gcloud container clusters get-credentials cnpm-food-gke --zone us-central1-a
```

### **Bước 3: Deploy**
```powershell
# Sửa mysql-deployment.yaml
# storageClassName: standard-rwo  # For GKE

# Deploy
kubectl create namespace cnpm-food
kubectl apply -f k8s/services/

# Get external IP
kubectl get svc -n cnpm-food
```

### **Chi phí ước tính GKE:**
- 3 x e2-medium instances: ~$75/tháng
- Load Balancer: ~$18/tháng
- Persistent Disk: ~$5/tháng
- **Tổng: ~$98/tháng**

---

## 🟠 Option 3: Amazon EKS

### **Bước 1: Cài AWS CLI và eksctl**
```powershell
# AWS CLI
msiexec.exe /i https://awscli.amazonaws.com/AWSCLIV2.msi

# eksctl
choco install eksctl
```

### **Bước 2: Tạo EKS cluster**
```powershell
# Configure AWS credentials
aws configure

# Tạo cluster với eksctl
eksctl create cluster \
  --name cnpm-food-eks \
  --region us-east-1 \
  --nodegroup-name standard-workers \
  --node-type t3.medium \
  --nodes 3 \
  --nodes-min 2 \
  --nodes-max 5 \
  --managed

# Update kubeconfig
aws eks update-kubeconfig --name cnpm-food-eks --region us-east-1
```

### **Bước 3: Deploy**
```powershell
# Sửa mysql-deployment.yaml
# storageClassName: gp2  # For EKS

kubectl create namespace cnpm-food
kubectl apply -f k8s/services/
```

### **Chi phí ước tính EKS:**
- EKS control plane: $73/tháng
- 3 x t3.medium EC2: ~$90/tháng
- ELB: ~$20/tháng
- EBS volumes: ~$5/tháng
- **Tổng: ~$188/tháng** (Đắt nhất!)

---

## 🆓 Option 4: Miễn phí - Kubernetes as a Service

### **DigitalOcean Kubernetes (DOKS)**
- 💰 $12/tháng cho 2-node cluster (cheapest)
- 🎁 $200 credit cho 60 ngày đầu
- 📚 Docs rất tốt, dễ setup

```powershell
# Install doctl
choco install doctl

# Auth
doctl auth init

# Tạo cluster
doctl kubernetes cluster create cnpm-food \
  --region sgp1 \
  --node-pool "name=worker-pool;size=s-2vcpu-4gb;count=2"

# Deploy
kubectl apply -f k8s/services/
```

### **Linode Kubernetes Engine (LKE)**
- 💰 $20/tháng cho 3-node cluster
- 🎁 $100 credit

### **Civo Cloud**
- 💰 $15/tháng cho 2-node cluster  
- 🎁 $250 credit cho 1 tháng
- ⚡ Launch nhanh nhất (< 2 phút)

---

## 📊 So sánh Cloud Providers

| Provider | Chi phí/tháng | Free Credit | Độ khó | Recommend |
|----------|---------------|-------------|---------|-----------|
| **DigitalOcean** | $12 | $200/60 days | ⭐ Easy | ✅ Best cho học tập |
| **Civo** | $15 | $250/30 days | ⭐ Easy | ✅ Nhanh nhất |
| **Azure AKS** | $85 | $200/30 days | ⭐⭐ Medium | ✅ Best cho production |
| **GKE** | $98 | $300/90 days | ⭐⭐ Medium | ⚠️ Credit nhiều nhất |
| **AWS EKS** | $188 | Không có | ⭐⭐⭐ Hard | ❌ Đắt nhất |
| **Linode** | $20 | $100 | ⭐ Easy | ⚠️ OK |

---

## 🎯 Khuyến nghị cho bạn

### **Nếu mục đích HỌC TẬP/DEMO:**
👉 **DigitalOcean** hoặc **Civo**
- Rẻ nhất
- Setup đơn giản nhất
- Credit đủ dùng 1-2 tháng

### **Nếu mục đích PRODUCTION thực tế:**
👉 **Azure AKS**
- Đã có GitHub Actions workflow sẵn
- Monitoring tốt
- Tích hợp Microsoft ecosystem

### **Nếu muốn FREE lâu dài:**
👉 **Google Cloud Platform**
- $300 credit cho 90 ngày
- Always Free tier (giới hạn)

---

## 🚀 Quick Start - DigitalOcean (Recommended)

```powershell
# 1. Đăng ký tài khoản
https://m.do.co/c/YOUR_REFERRAL_CODE
# → Nhận $200 credit

# 2. Cài doctl
choco install doctl

# 3. Authenticate
doctl auth init
# → Nhập API token từ DigitalOcean dashboard

# 4. Tạo cluster
doctl kubernetes cluster create cnpm-food-cluster \
  --region sgp1 \
  --size s-2vcpu-4gb \
  --count 2

# 5. Deploy
kubectl create namespace cnpm-food
kubectl apply -f k8s/services/mysql-deployment.yaml
kubectl wait --for=condition=ready pod -l app=mysql -n cnpm-food --timeout=300s
kubectl apply -f k8s/services/

# 6. Get Load Balancer IP
kubectl get svc -n cnpm-food

# 7. Access app
# Frontend: http://<FRONTEND-EXTERNAL-IP>
# API Gateway: http://<API-GATEWAY-EXTERNAL-IP>:9000
```

---

## 📝 Checklist Deploy lên Cloud

- [ ] Chọn cloud provider
- [ ] Đăng ký tài khoản + nhận free credit
- [ ] Cài CLI tools (az/gcloud/doctl)
- [ ] Tạo Kubernetes cluster
- [ ] Update kubeconfig để connect
- [ ] (Tùy chọn) Setup container registry
- [ ] Update `storageClassName` trong mysql-deployment.yaml
- [ ] Deploy MySQL trước
- [ ] Deploy các services còn lại
- [ ] Kiểm tra pods status
- [ ] Lấy external IP của LoadBalancer
- [ ] Test application
- [ ] Setup monitoring (Prometheus/Grafana)
- [ ] Setup CI/CD với GitHub Actions

---

## ⚠️ Lưu ý quan trọng

### **1. StorageClass khác nhau giữa các cloud:**
- Docker Desktop: Không cần (hoặc để trống)
- Azure AKS: `managed-csi`
- GKE: `standard-rwo`
- AWS EKS: `gp2`
- DigitalOcean: `do-block-storage`

### **2. LoadBalancer sẽ tốn phí:**
- Azure: ~$20/tháng
- GCP: ~$18/tháng
- AWS: ~$20/tháng
- DigitalOcean: $12/tháng/LB

### **3. Persistent Volumes giữ lại data:**
```powershell
# Xóa cluster KHÔNG tự động xóa PV/PVC
# Phải xóa manual để tránh tính tiền:
kubectl delete pvc --all -n cnpm-food
```

### **4. Scale down khi không dùng:**
```powershell
# Scale deployments về 0 để tiết kiệm (vẫn tốn tiền cluster)
kubectl scale deployment --all --replicas=0 -n cnpm-food

# Scale lại khi cần
kubectl scale deployment --all --replicas=1 -n cnpm-food
```

### **5. Monitoring tiêu tốn resources:**
Prometheus + Grafana cần thêm ~1-2GB RAM → tăng chi phí nodes

---

## 🆘 Troubleshooting Cloud Deployment

### **Pods không start:**
```powershell
kubectl describe pod <pod-name> -n cnpm-food
kubectl logs <pod-name> -n cnpm-food
```

### **PVC Pending:**
```powershell
# Kiểm tra StorageClass
kubectl get storageclass

# Update mysql-deployment.yaml với đúng storageClassName
```

### **LoadBalancer IP Pending:**
```powershell
# Kiểm tra cloud provider có hỗ trợ LoadBalancer không
kubectl get svc -n cnpm-food

# Nếu không có external IP sau 5 phút → dùng NodePort hoặc Ingress
```

### **Out of CPU/Memory:**
```powershell
# Giảm resource requests trong deployment files
resources:
  requests:
    cpu: "100m"  # Giảm từ 250m
    memory: "256Mi"  # Giảm từ 512Mi
```

---

## 📚 Tài liệu tham khảo

- [Azure AKS Documentation](https://docs.microsoft.com/en-us/azure/aks/)
- [GKE Documentation](https://cloud.google.com/kubernetes-engine/docs)
- [AWS EKS Documentation](https://docs.aws.amazon.com/eks/)
- [DigitalOcean Kubernetes](https://docs.digitalocean.com/products/kubernetes/)
- [Civo Cloud](https://www.civo.com/docs/kubernetes)

---

**Bạn muốn deploy lên cloud nào? Tôi sẽ hướng dẫn chi tiết từng bước!**
