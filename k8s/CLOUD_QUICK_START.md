# ⚡ Quick Deploy to Cloud - Cheat Sheet

## 🚀 Option 1: DigitalOcean (FASTEST & CHEAPEST)

```powershell
# 1. Install doctl
choco install doctl

# 2. Login
doctl auth init
# → Paste API token từ https://cloud.digitalocean.com/account/api/tokens

# 3. Create cluster (2 phút)
doctl kubernetes cluster create cnpm-food --region sgp1 --size s-2vcpu-4gb --count 2

# 4. Deploy
cd k8s
kubectl create namespace cnpm-food
kubectl apply -f services/mysql-deployment.yaml
kubectl wait --for=condition=ready pod -l app=mysql -n cnpm-food --timeout=300s
kubectl apply -f services/

# 5. Get external IP
kubectl get svc -n cnpm-food
```

**Cost**: $72/month ($200 credit = 2+ tháng free)

---

## 🔵 Option 2: Azure AKS (RECOMMENDED FOR PRODUCTION)

```powershell
# 1. Install Azure CLI
winget install Microsoft.AzureCLI

# 2. Login
az login

# 3. Create resources
az group create --name cnpm-food-rg --location eastus
az aks create --resource-group cnpm-food-rg --name cnpm-food-aks --node-count 3 --node-vm-size Standard_B2s

# 4. Get credentials
az aks get-credentials --resource-group cnpm-food-rg --name cnpm-food-aks

# 5. Update MySQL for Azure
cd k8s/services
# Uncomment trong mysql-deployment.yaml:
# storageClassName: managed-csi

# 6. Deploy
cd ..
kubectl create namespace cnpm-food
kubectl apply -f services/

# 7. Get external IP
kubectl get svc -n cnpm-food
```

**Cost**: $130/month ($200 credit = 1+ tháng free)

---

## 🔴 Option 3: Google Cloud GKE

```powershell
# 1. Install gcloud SDK
# Download: https://cloud.google.com/sdk/docs/install

# 2. Init & login
gcloud init
gcloud auth login

# 3. Set project
gcloud config set project YOUR_PROJECT_ID

# 4. Enable API
gcloud services enable container.googleapis.com

# 5. Create cluster
gcloud container clusters create cnpm-food --zone us-central1-a --num-nodes 3 --machine-type e2-medium

# 6. Get credentials
gcloud container clusters get-credentials cnpm-food --zone us-central1-a

# 7. Update MySQL for GKE
cd k8s/services
# Sửa mysql-deployment.yaml:
# storageClassName: standard-rwo

# 8. Deploy
cd ..
kubectl create namespace cnpm-food
kubectl apply -f services/

# 9. Get external IP
kubectl get svc -n cnpm-food
```

**Cost**: $189/month ($300 credit = 1.5+ tháng free)

---

## 📦 Using PowerShell Script (AUTO)

```powershell
cd k8s

# Deploy to DigitalOcean
.\deploy-to-cloud.ps1 -CloudProvider digitalocean

# Deploy to Azure
.\deploy-to-cloud.ps1 -CloudProvider azure -Region eastus

# Deploy to GCP
.\deploy-to-cloud.ps1 -CloudProvider gcp -Region us-central1-a
```

---

## 🔍 Verification Commands

```powershell
# Check cluster info
kubectl cluster-info

# Check nodes
kubectl get nodes

# Check all pods
kubectl get pods -n cnpm-food

# Check services and external IPs
kubectl get svc -n cnpm-food

# Check deployments
kubectl get deployments -n cnpm-food

# Watch pods in real-time
kubectl get pods -n cnpm-food -w
```

---

## 🐛 Troubleshooting

### Pods không start
```powershell
kubectl describe pod <pod-name> -n cnpm-food
kubectl logs <pod-name> -n cnpm-food
```

### PVC Pending
```powershell
# Check storage class
kubectl get storageclass

# Sửa mysql-deployment.yaml với đúng storageClassName:
# - Azure: managed-csi
# - GCP: standard-rwo
# - DO: do-block-storage
```

### LoadBalancer IP Pending
```powershell
# Đợi 2-5 phút cho cloud provision
kubectl get svc -n cnpm-food -w

# Nếu vẫn pending sau 10 phút, check events
kubectl describe svc frontend -n cnpm-food
```

### Out of Resources
```powershell
# Scale down replicas
kubectl scale deployment --all --replicas=1 -n cnpm-food

# Hoặc reduce resource requests trong deployment files
```

---

## 🧹 Cleanup (Xóa để tránh tính phí)

### DigitalOcean
```powershell
doctl kubernetes cluster delete cnpm-food
```

### Azure
```powershell
az group delete --name cnpm-food-rg --yes --no-wait
```

### GCP
```powershell
gcloud container clusters delete cnpm-food --zone us-central1-a
```

---

## 💡 Pro Tips

1. **Scale down khi không dùng**:
```powershell
kubectl scale deployment --all --replicas=0 -n cnpm-food
```

2. **Scale up khi cần**:
```powershell
kubectl scale deployment --all --replicas=1 -n cnpm-food
```

3. **Export external IPs**:
```powershell
$frontendIP = kubectl get svc frontend -n cnpm-food -o jsonpath='{.status.loadBalancer.ingress[0].ip}'
$gatewayIP = kubectl get svc api-gateway -n cnpm-food -o jsonpath='{.status.loadBalancer.ingress[0].ip}'

Write-Host "Frontend: http://$frontendIP"
Write-Host "API Gateway: http://$gatewayIP:9000"
```

4. **Port forward for testing** (nếu chưa có LoadBalancer):
```powershell
kubectl port-forward svc/frontend 4200:80 -n cnpm-food
kubectl port-forward svc/api-gateway 9000:9000 -n cnpm-food
```

---

## 📚 More Info

- Full guide: `CLOUD_DEPLOYMENT_GUIDE.md`
- Cost calculator: `CLOUD_COST_CALCULATOR.md`
- Local deployment: `START_HERE.md`
