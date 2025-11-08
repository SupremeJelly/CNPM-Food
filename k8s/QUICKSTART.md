# 🚀 Quick Start - Deploy to Kubernetes

## Option 1: Using Docker Hub (Easiest - No Azure needed)

### 1. Build and Push Images
```powershell
# Set your Docker Hub username
$DOCKER_USERNAME = "yourusername"

# Login to Docker Hub
docker login

# Build and push (run from d:\CNPM-Food)
cd d:\CNPM-Food

docker build -t $DOCKER_USERNAME/cnpm-user-service:latest ./user-service
docker push $DOCKER_USERNAME/cnpm-user-service:latest

docker build -t $DOCKER_USERNAME/cnpm-restaurant-service:latest ./restaurant-service
docker push $DOCKER_USERNAME/cnpm-restaurant-service:latest

docker build -t $DOCKER_USERNAME/cnpm-order-service:latest ./order-service
docker push $DOCKER_USERNAME/cnpm-order-service:latest

docker build -t $DOCKER_USERNAME/cnpm-payment-service:latest ./payment-service
docker push $DOCKER_USERNAME/cnpm-payment-service:latest

docker build -t $DOCKER_USERNAME/cnpm-api-gateway:latest ./api-gateway
docker push $DOCKER_USERNAME/cnpm-api-gateway:latest

docker build -t $DOCKER_USERNAME/cnpm-frontend:latest ./frontend
docker push $DOCKER_USERNAME/cnpm-frontend:latest
```

### 2. Update Deployment Files
Replace `cnpmfoodacr.azurecr.io` with `yourusername` in all files:
- `k8s/services/user-service-deployment.yaml`
- `k8s/services/restaurant-service-deployment.yaml`
- `k8s/services/order-service-deployment.yaml`
- `k8s/services/payment-service-deployment.yaml`
- `k8s/services/api-gateway-deployment.yaml`
- `k8s/services/frontend-deployment.yaml`

### 3. Deploy to Kubernetes
```powershell
# Create namespace
kubectl create namespace cnpm-food

# Deploy MySQL
kubectl apply -f k8s/services/mysql-deployment.yaml

# Wait for MySQL
kubectl wait --for=condition=ready pod -l app=mysql -n cnpm-food --timeout=300s

# Deploy all services
kubectl apply -f k8s/services/user-service-deployment.yaml
kubectl apply -f k8s/services/restaurant-service-deployment.yaml
kubectl apply -f k8s/services/order-service-deployment.yaml
kubectl apply -f k8s/services/payment-service-deployment.yaml
kubectl apply -f k8s/services/api-gateway-deployment.yaml
kubectl apply -f k8s/services/frontend-deployment.yaml

# Check status
kubectl get pods -n cnpm-food
```

### 4. Install Monitoring
```powershell
# Add Helm repo
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install monitoring stack
helm install monitoring prometheus-community/kube-prometheus-stack `
  --namespace monitoring `
  --create-namespace `
  --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false `
  --set grafana.adminPassword=admin `
  --wait

# Apply ServiceMonitors
kubectl apply -f k8s/monitoring/servicemonitor.yaml
```

### 5. Access Applications
```powershell
# Frontend
kubectl port-forward svc/frontend -n cnpm-food 4200:80
# Open: http://localhost:4200

# API Gateway
kubectl port-forward svc/api-gateway -n cnpm-food 9000:9000
# Open: http://localhost:9000

# Grafana
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80
# Open: http://localhost:3000 (admin/admin)

# Prometheus
kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090:9090
# Open: http://localhost:9090
```

---

## Option 2: Using Azure AKS (Production Ready)

### 1. Create Azure Resources
```powershell
# Login to Azure
az login

# Create Resource Group
az group create --name cnpm-food-rg --location southeastasia

# Create Azure Container Registry
az acr create --resource-group cnpm-food-rg --name cnpmfoodacr --sku Basic

# Login to ACR
az acr login --name cnpmfoodacr

# Create AKS Cluster (this takes ~10 minutes)
az aks create `
  --resource-group cnpm-food-rg `
  --name cnpm-food-aks `
  --node-count 2 `
  --node-vm-size Standard_B2s `
  --enable-addons monitoring `
  --generate-ssh-keys `
  --attach-acr cnpmfoodacr

# Get AKS credentials
az aks get-credentials --resource-group cnpm-food-rg --name cnpm-food-aks

# Verify connection
kubectl get nodes
```

### 2. Build and Push to ACR
```powershell
cd d:\CNPM-Food

docker build -t cnpmfoodacr.azurecr.io/user-service:latest ./user-service
docker push cnpmfoodacr.azurecr.io/user-service:latest

docker build -t cnpmfoodacr.azurecr.io/restaurant-service:latest ./restaurant-service
docker push cnpmfoodacr.azurecr.io/restaurant-service:latest

docker build -t cnpmfoodacr.azurecr.io/order-service:latest ./order-service
docker push cnpmfoodacr.azurecr.io/order-service:latest

docker build -t cnpmfoodacr.azurecr.io/payment-service:latest ./payment-service
docker push cnpmfoodacr.azurecr.io/payment-service:latest

docker build -t cnpmfoodacr.azurecr.io/api-gateway:latest ./api-gateway
docker push cnpmfoodacr.azurecr.io/api-gateway:latest

docker build -t cnpmfoodacr.azurecr.io/frontend:latest ./frontend
docker push cnpmfoodacr.azurecr.io/frontend:latest
```

### 3. Deploy Everything
```powershell
# Use automated script
.\k8s\deploy.ps1
```

---

## Option 3: Using Automated Script (Recommended)

### For Windows:
```powershell
cd d:\CNPM-Food

# Full deployment (Docker Hub)
.\k8s\deploy.ps1 -Registry "yourusername"

# Full deployment (Azure ACR)
.\k8s\deploy.ps1 -Registry "cnpmfoodacr.azurecr.io"

# Skip build (if images already built)
.\k8s\deploy.ps1 -Registry "yourusername" -SkipBuild

# Skip monitoring
.\k8s\deploy.ps1 -Registry "yourusername" -SkipMonitoring
```

### For Linux/Mac:
```bash
cd /d/CNPM-Food

# Make script executable
chmod +x k8s/deploy.sh

# Run deployment
./k8s/deploy.sh
```

---

## Verify Deployment

### Check Pods
```powershell
kubectl get pods -n cnpm-food
```

Expected output:
```
NAME                                   READY   STATUS    RESTARTS   AGE
mysql-xxxxx                            1/1     Running   0          2m
user-service-xxxxx                     1/1     Running   0          1m
restaurant-service-xxxxx               1/1     Running   0          1m
order-service-xxxxx                    1/1     Running   0          1m
payment-service-xxxxx                  1/1     Running   0          1m
api-gateway-xxxxx                      1/1     Running   0          1m
frontend-xxxxx                         1/1     Running   0          1m
```

### Check Services
```powershell
kubectl get svc -n cnpm-food
```

### Check Logs
```powershell
# Check user-service logs
kubectl logs -f deployment/user-service -n cnpm-food

# Check all pods
kubectl logs -l app=user-service -n cnpm-food
```

### Check Monitoring
```powershell
kubectl get pods -n monitoring
```

---

## Troubleshooting

### Pod not starting?
```powershell
# Describe pod
kubectl describe pod <pod-name> -n cnpm-food

# Check events
kubectl get events -n cnpm-food --sort-by='.lastTimestamp'
```

### ImagePullBackOff?
```powershell
# For Docker Hub: make sure images are public
# For ACR: check ACR integration
az aks check-acr --resource-group cnpm-food-rg --name cnpm-food-aks --acr cnpmfoodacr.azurecr.io
```

### Service not accessible?
```powershell
# Check endpoints
kubectl get endpoints -n cnpm-food

# Test from another pod
kubectl run -it --rm debug --image=curlimages/curl --restart=Never -n cnpm-food -- curl http://user-service:8081/actuator/health
```

---

## Clean Up

### Delete everything:
```powershell
# Delete namespace (deletes all resources)
kubectl delete namespace cnpm-food
kubectl delete namespace monitoring

# For Azure AKS - delete resource group
az group delete --name cnpm-food-rg --yes --no-wait
```

---

## Cost Estimate (Azure AKS)

- **2 x Standard_B2s nodes**: ~$60/month
- **Azure Container Registry**: ~$5/month
- **Load Balancer**: ~$20/month (if using)
- **Storage**: ~$5/month
- **Total**: ~$90/month

**Free alternatives:**
- Use Docker Hub instead of ACR: -$5/month
- Use 1 node cluster: -$30/month
- Use NodePort instead of LoadBalancer: -$20/month
- **Minimum cost**: ~$35/month

---

## Next Steps

1. ✅ Import Grafana dashboards (11378, 4701, 6756)
2. ✅ Configure domain name (optional)
3. ✅ Set up HTTPS with cert-manager (optional)
4. ✅ Configure autoscaling (HPA)
5. ✅ Set up CI/CD pipeline (GitHub Actions)

**Happy Deploying! 🚀**
