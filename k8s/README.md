# 🚀 Kubernetes Deployment Guide - CNPM Food System

## 📋 Table of Contents
1. [Prerequisites](#prerequisites)
2. [Docker Images Build & Push](#docker-images-build--push)
3. [Azure Kubernetes Service (AKS) Setup](#azure-kubernetes-service-aks-setup)
4. [Deploy to Kubernetes](#deploy-to-kubernetes)
5. [Monitoring Setup](#monitoring-setup)
6. [Access Applications](#access-applications)
7. [Troubleshooting](#troubleshooting)

---

## 1. Prerequisites

### Required Tools
```bash
# Install Azure CLI
choco install azure-cli

# Install kubectl
choco install kubernetes-cli

# Install Helm (for Prometheus/Grafana)
choco install kubernetes-helm

# Verify installations
az --version
kubectl version --client
helm version
```

### Azure Account
- Azure subscription (Free trial: https://azure.microsoft.com/free/)
- Resource Group created
- Azure Container Registry (ACR) created

---

## 2. Docker Images Build & Push

### Step 1: Login to Docker Hub (Option 1) or Azure ACR (Option 2)

#### Option 1: Docker Hub (Free)
```bash
# Login to Docker Hub
docker login

# Set your Docker Hub username
$DOCKER_USERNAME = "your-dockerhub-username"
```

#### Option 2: Azure Container Registry (Recommended)
```bash
# Login to Azure
az login

# Create Resource Group
az group create --name cnpm-food-rg --location southeastasia

# Create Azure Container Registry
az acr create --resource-group cnpm-food-rg --name cnpmfoodacr --sku Basic

# Login to ACR
az acr login --name cnpmfoodacr

# Get ACR login server
$ACR_LOGIN_SERVER = az acr show --name cnpmfoodacr --query loginServer --output tsv
# Result: cnpmfoodacr.azurecr.io
```

### Step 2: Build and Push Images

#### Using Docker Hub:
```bash
# Build and push all services
cd d:\CNPM-Food

# User Service
docker build -t $DOCKER_USERNAME/cnpm-user-service:latest ./user-service
docker push $DOCKER_USERNAME/cnpm-user-service:latest

# Restaurant Service
docker build -t $DOCKER_USERNAME/cnpm-restaurant-service:latest ./restaurant-service
docker push $DOCKER_USERNAME/cnpm-restaurant-service:latest

# Order Service
docker build -t $DOCKER_USERNAME/cnpm-order-service:latest ./order-service
docker push $DOCKER_USERNAME/cnpm-order-service:latest

# Payment Service
docker build -t $DOCKER_USERNAME/cnpm-payment-service:latest ./payment-service
docker push $DOCKER_USERNAME/cnpm-payment-service:latest

# API Gateway
docker build -t $DOCKER_USERNAME/cnpm-api-gateway:latest ./api-gateway
docker push $DOCKER_USERNAME/cnpm-api-gateway:latest

# Frontend
docker build -t $DOCKER_USERNAME/cnpm-frontend:latest ./frontend
docker push $DOCKER_USERNAME/cnpm-frontend:latest
```

#### Using Azure ACR:
```bash
# Build and push all services
cd d:\CNPM-Food

# User Service
docker build -t cnpmfoodacr.azurecr.io/user-service:latest ./user-service
docker push cnpmfoodacr.azurecr.io/user-service:latest

# Restaurant Service
docker build -t cnpmfoodacr.azurecr.io/restaurant-service:latest ./restaurant-service
docker push cnpmfoodacr.azurecr.io/restaurant-service:latest

# Order Service
docker build -t cnpmfoodacr.azurecr.io/order-service:latest ./order-service
docker push cnpmfoodacr.azurecr.io/order-service:latest

# Payment Service
docker build -t cnpmfoodacr.azurecr.io/payment-service:latest ./payment-service
docker push cnpmfoodacr.azurecr.io/payment-service:latest

# API Gateway
docker build -t cnpmfoodacr.azurecr.io/api-gateway:latest ./api-gateway
docker push cnpmfoodacr.azurecr.io/api-gateway:latest

# Frontend
docker build -t cnpmfoodacr.azurecr.io/frontend:latest ./frontend
docker push cnpmfoodacr.azurecr.io/frontend:latest
```

---

## 3. Azure Kubernetes Service (AKS) Setup

### Step 1: Create AKS Cluster
```bash
# Create AKS cluster (2 nodes, Standard_B2s)
az aks create \
  --resource-group cnpm-food-rg \
  --name cnpm-food-aks \
  --node-count 2 \
  --node-vm-size Standard_B2s \
  --enable-addons monitoring \
  --generate-ssh-keys \
  --attach-acr cnpmfoodacr

# This takes ~10 minutes
```

### Step 2: Connect to AKS
```bash
# Get credentials
az aks get-credentials --resource-group cnpm-food-rg --name cnpm-food-aks

# Verify connection
kubectl get nodes
# Should show 2 nodes in Ready state
```

### Step 3: Create Namespace
```bash
kubectl create namespace cnpm-food
kubectl config set-context --current --namespace=cnpm-food
```

---

## 4. Deploy to Kubernetes

### Step 1: Update Image Names in Deployments

**If using Docker Hub**, edit all deployment files in `k8s/services/` and replace:
```yaml
image: cnpmfoodacr.azurecr.io/user-service:latest
```
with:
```yaml
image: your-dockerhub-username/cnpm-user-service:latest
```

### Step 2: Deploy MySQL
```bash
kubectl apply -f k8s/services/mysql-deployment.yaml
kubectl apply -f k8s/services/mysql-service.yaml

# Wait for MySQL to be ready
kubectl wait --for=condition=ready pod -l app=mysql --timeout=300s
```

### Step 3: Deploy Microservices
```bash
# Deploy all services
kubectl apply -f k8s/services/user-service-deployment.yaml
kubectl apply -f k8s/services/restaurant-service-deployment.yaml
kubectl apply -f k8s/services/order-service-deployment.yaml
kubectl apply -f k8s/services/payment-service-deployment.yaml
kubectl apply -f k8s/services/api-gateway-deployment.yaml
kubectl apply -f k8s/services/frontend-deployment.yaml

# Check status
kubectl get pods
kubectl get services
```

---

## 5. Monitoring Setup

### Option 1: Using Helm (Recommended - Easiest)

#### Install Prometheus + Grafana Stack
```bash
# Add Prometheus community Helm repo
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install kube-prometheus-stack (includes Prometheus, Grafana, Alertmanager)
helm install monitoring prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false \
  --set grafana.adminPassword=admin

# Wait for pods
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=grafana -n monitoring --timeout=300s
```

#### Access Grafana
```bash
# Port forward Grafana
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80

# Open browser: http://localhost:3000
# Login: admin / admin
```

#### Access Prometheus
```bash
# Port forward Prometheus
kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090:9090

# Open browser: http://localhost:9090
```

### Option 2: Using Custom Manifests (Manual)

```bash
# Deploy Prometheus
kubectl apply -f k8s/monitoring/prometheus-config.yaml
kubectl apply -f k8s/monitoring/prometheus-deployment.yaml
kubectl apply -f k8s/monitoring/prometheus-service.yaml

# Deploy Grafana
kubectl apply -f k8s/monitoring/grafana-deployment.yaml
kubectl apply -f k8s/monitoring/grafana-service.yaml

# Check monitoring pods
kubectl get pods -n monitoring
```

### Configure ServiceMonitors (for Helm deployment)

Apply custom ServiceMonitors for your services:
```bash
kubectl apply -f k8s/monitoring/servicemonitor.yaml
```

---

## 6. Access Applications

### Option 1: Port Forward (Development)
```bash
# Frontend
kubectl port-forward svc/frontend 4200:4200

# API Gateway
kubectl port-forward svc/api-gateway 9000:9000

# Grafana
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80

# Prometheus
kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090:9090
```

### Option 2: LoadBalancer (Production)

Edit service files to change `type: ClusterIP` to `type: LoadBalancer`:
```bash
kubectl patch svc frontend -p '{"spec":{"type":"LoadBalancer"}}'
kubectl patch svc api-gateway -p '{"spec":{"type":"LoadBalancer"}}'

# Get external IPs
kubectl get svc
```

### Option 3: Ingress Controller (Recommended for Production)

```bash
# Install NGINX Ingress Controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/cloud/deploy.yaml

# Apply ingress rules
kubectl apply -f k8s/ingress.yaml

# Get ingress IP
kubectl get ingress
```

---

## 7. Troubleshooting

### Check Pods Status
```bash
# List all pods
kubectl get pods

# Describe pod
kubectl describe pod <pod-name>

# Check logs
kubectl logs <pod-name>
kubectl logs -f <pod-name>  # Follow logs

# Check previous crashed pod logs
kubectl logs <pod-name> --previous
```

### Check Services
```bash
# List services
kubectl get svc

# Check service endpoints
kubectl get endpoints

# Describe service
kubectl describe svc <service-name>
```

### Common Issues

#### 1. ImagePullBackOff
```bash
# Check if image exists in registry
docker pull cnpmfoodacr.azurecr.io/user-service:latest

# Check ACR integration
az aks check-acr --resource-group cnpm-food-rg --name cnpm-food-aks --acr cnpmfoodacr.azurecr.io
```

#### 2. CrashLoopBackOff
```bash
# Check logs
kubectl logs <pod-name>

# Check MySQL connection
kubectl exec -it <pod-name> -- curl mysql:3306
```

#### 3. Service Not Accessible
```bash
# Check service endpoints
kubectl get endpoints <service-name>

# Test service from another pod
kubectl run -it --rm debug --image=curlimages/curl --restart=Never -- curl http://user-service:8081/actuator/health
```

### Restart Services
```bash
# Restart deployment
kubectl rollout restart deployment user-service

# Scale down and up
kubectl scale deployment user-service --replicas=0
kubectl scale deployment user-service --replicas=1
```

### Delete and Redeploy
```bash
# Delete all resources
kubectl delete -f k8s/services/

# Redeploy
kubectl apply -f k8s/services/
```

---

## 📊 Monitoring Dashboards

### Import Grafana Dashboards

1. Login to Grafana (http://localhost:3000)
2. Click **+** → **Import**
3. Import these dashboards:
   - **11378** - Spring Boot Statistics
   - **4701** - JVM Micrometer
   - **6756** - Spring Boot Stats
   - **13770** - Kubernetes Cluster Monitoring
   - **6417** - Kubernetes Deployment Metrics

### Custom Prometheus Queries

#### Pod CPU Usage
```promql
rate(container_cpu_usage_seconds_total{namespace="cnpm-food"}[5m])
```

#### Pod Memory Usage
```promql
container_memory_usage_bytes{namespace="cnpm-food"}
```

#### HTTP Request Rate
```promql
rate(http_server_requests_seconds_count{application="user-service"}[5m])
```

---

## 🔐 Security Best Practices

### 1. Use Secrets for Sensitive Data
```bash
# Create secret for MySQL
kubectl create secret generic mysql-secret \
  --from-literal=root-password=rootpass \
  --from-literal=user-password=apassword

# Use in deployment
env:
  - name: MYSQL_ROOT_PASSWORD
    valueFrom:
      secretKeyRef:
        name: mysql-secret
        key: root-password
```

### 2. Enable RBAC
```bash
kubectl apply -f k8s/rbac.yaml
```

### 3. Network Policies
```bash
kubectl apply -f k8s/network-policy.yaml
```

---

## 📈 Scaling

### Manual Scaling
```bash
# Scale user-service to 3 replicas
kubectl scale deployment user-service --replicas=3
```

### Horizontal Pod Autoscaler (HPA)
```bash
# Enable metrics server (if not already)
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# Create HPA
kubectl autoscale deployment user-service --cpu-percent=70 --min=1 --max=10

# Check HPA
kubectl get hpa
```

---

## 💰 Cost Optimization

### Azure AKS Costs (Approximate)
- **2 x Standard_B2s nodes**: ~$60/month
- **Azure Container Registry (Basic)**: ~$5/month
- **Load Balancer**: ~$20/month
- **Storage**: ~$5/month
- **Total**: ~$90/month

### Free Tier Options
- Use **Docker Hub** instead of ACR (free for public repos)
- Use **1 node cluster** for development
- Use **NodePort** instead of LoadBalancer
- Use **Azure Free Trial** ($200 credit for 30 days)

---

## 🎯 Quick Commands Summary

```bash
# Build and push images (Docker Hub)
docker build -t yourusername/cnpm-user-service:latest ./user-service && docker push yourusername/cnpm-user-service:latest

# Deploy all services
kubectl apply -f k8s/services/

# Check status
kubectl get pods,svc

# View logs
kubectl logs -f deployment/user-service

# Access applications
kubectl port-forward svc/frontend 4200:4200
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80

# Clean up
kubectl delete namespace cnpm-food
kubectl delete namespace monitoring
```

---

## 📞 Support & Resources

- **Kubernetes Docs**: https://kubernetes.io/docs/
- **Azure AKS Docs**: https://learn.microsoft.com/en-us/azure/aks/
- **Helm Charts**: https://artifacthub.io/
- **Prometheus Docs**: https://prometheus.io/docs/
- **Grafana Docs**: https://grafana.com/docs/

---

**Last Updated**: 2025-11-07
**Status**: Ready for Deployment 🚀
