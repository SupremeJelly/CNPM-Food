# 📦 Kubernetes Deployment Summary

## ✅ What Has Been Created

### 1. Kubernetes Manifests
```
k8s/
├── README.md                          # Complete deployment guide
├── QUICKSTART.md                      # Quick start guide
├── deploy.sh                          # Bash deployment script
├── deploy.ps1                         # PowerShell deployment script
├── hpa.yaml                          # Horizontal Pod Autoscaler
├── ingress.yaml                      # Ingress controller config
├── services/
│   ├── mysql-deployment.yaml         # MySQL StatefulSet + PVC
│   ├── user-service-deployment.yaml
│   ├── restaurant-service-deployment.yaml
│   ├── order-service-deployment.yaml
│   ├── payment-service-deployment.yaml
│   ├── api-gateway-deployment.yaml
│   └── frontend-deployment.yaml
└── monitoring/
    └── servicemonitor.yaml           # Prometheus ServiceMonitors
```

### 2. CI/CD Pipeline
```
.github/workflows/
└── deploy-k8s.yml                    # GitHub Actions workflow
```

---

## 🎯 Deployment Options

### Option 1: Docker Hub (FREE - No Cloud Required)
**Best for**: Testing, Development, Small Projects

**Pros:**
- ✅ Completely FREE
- ✅ No Azure account needed
- ✅ Public images accessible anywhere
- ✅ Works with any Kubernetes (Minikube, Kind, etc.)

**Cons:**
- ❌ Images are public (security concern)
- ❌ Slower pulls from Asia region
- ❌ Limited to 1 private repo on free tier

**Cost**: $0/month

### Option 2: Azure AKS + ACR (RECOMMENDED for Production)
**Best for**: Production, Enterprise, Scalable Apps

**Pros:**
- ✅ Private container registry
- ✅ Fast image pulls (same region)
- ✅ Integrated monitoring (Azure Monitor)
- ✅ Auto-scaling support
- ✅ Professional support

**Cons:**
- ❌ Costs money (~$90/month)
- ❌ Requires Azure account
- ❌ More complex setup

**Cost**: ~$90/month (can reduce to ~$35/month)

### Option 3: Local Kubernetes (FREE - For Development)
**Best for**: Local testing before deploying to cloud

**Options:**
- **Minikube**: Single-node cluster
- **Kind**: Kubernetes in Docker
- **Docker Desktop**: Built-in Kubernetes

**Cost**: $0/month

---

## 🚀 Quick Deploy Steps

### For Docker Hub (Easiest):

1. **Build & Push Images**
```powershell
docker login
$USERNAME = "yourusername"

docker build -t $USERNAME/cnpm-user-service:latest ./user-service
docker push $USERNAME/cnpm-user-service:latest
# Repeat for all 6 services
```

2. **Update Image Names**
Edit `k8s/services/*-deployment.yaml`, replace:
```yaml
image: cnpmfoodacr.azurecr.io/user-service:latest
```
with:
```yaml
image: yourusername/cnpm-user-service:latest
```

3. **Deploy**
```powershell
kubectl create namespace cnpm-food
kubectl apply -f k8s/services/mysql-deployment.yaml
kubectl wait --for=condition=ready pod -l app=mysql -n cnpm-food --timeout=300s
kubectl apply -f k8s/services/
```

4. **Install Monitoring**
```powershell
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install monitoring prometheus-community/kube-prometheus-stack --namespace monitoring --create-namespace
kubectl apply -f k8s/monitoring/servicemonitor.yaml
```

5. **Access Apps**
```powershell
kubectl port-forward svc/frontend -n cnpm-food 4200:80
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80
```

---

### For Azure AKS:

1. **Create Azure Resources**
```powershell
az login
az group create --name cnpm-food-rg --location southeastasia
az acr create --resource-group cnpm-food-rg --name cnpmfoodacr --sku Basic
az aks create --resource-group cnpm-food-rg --name cnpm-food-aks --node-count 2 --attach-acr cnpmfoodacr
az aks get-credentials --resource-group cnpm-food-rg --name cnpm-food-aks
```

2. **Use Automated Script**
```powershell
cd d:\CNPM-Food
.\k8s\deploy.ps1 -Registry "cnpmfoodacr.azurecr.io"
```

---

## 📊 Monitoring Setup

### Prometheus + Grafana (Included)
Monitoring stack is deployed with Helm:
- **Prometheus**: Metrics collection
- **Grafana**: Visualization dashboards
- **Alertmanager**: Alert notifications
- **ServiceMonitors**: Auto-discovery of services

### Access Monitoring:
```powershell
# Grafana (admin/admin)
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80
# http://localhost:3000

# Prometheus
kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090:9090
# http://localhost:9090
```

### Import Dashboards:
1. Login to Grafana
2. Click **+** → **Import**
3. Import these dashboards:
   - **11378** - Spring Boot Statistics
   - **4701** - JVM Micrometer
   - **13770** - Kubernetes Cluster Monitoring

---

## 🔄 CI/CD Pipeline (GitHub Actions)

### Setup:
1. **Create GitHub Secrets**:
   - `ACR_USERNAME`: Azure Container Registry username
   - `ACR_PASSWORD`: Azure Container Registry password
   - `AZURE_CREDENTIALS`: Azure service principal JSON

2. **Get Azure Credentials**:
```bash
az ad sp create-for-rbac --name "cnpm-food-deploy" \
  --role contributor \
  --scopes /subscriptions/{subscription-id}/resourceGroups/cnpm-food-rg \
  --sdk-auth
```

3. **Enable Workflow**:
   - Push code to `main` branch
   - GitHub Actions will automatically:
     - Build Docker images
     - Push to registry
     - Deploy to Kubernetes
     - Run health checks

---

## 📈 Auto-Scaling (HPA)

Horizontal Pod Autoscaler is configured to scale based on:
- **CPU**: 70% threshold
- **Memory**: 80% threshold
- **Min Replicas**: 2
- **Max Replicas**: 10

### Apply HPA:
```powershell
kubectl apply -f k8s/hpa.yaml
kubectl get hpa -n cnpm-food
```

---

## 🔐 Security Best Practices

### 1. Use Kubernetes Secrets
```powershell
kubectl create secret generic mysql-secret \
  --from-literal=root-password=rootpass \
  --from-literal=user-password=apassword \
  -n cnpm-food
```

### 2. Network Policies (TODO)
Restrict pod-to-pod communication

### 3. RBAC (TODO)
Role-based access control

### 4. Image Scanning (TODO)
Scan images for vulnerabilities

---

## 💰 Cost Comparison

### Azure AKS - Standard Setup:
| Resource | Cost (Monthly) |
|----------|----------------|
| 2x Standard_B2s nodes | $60 |
| Azure Container Registry (Basic) | $5 |
| Load Balancer | $20 |
| Storage (10GB) | $2 |
| **Total** | **~$87** |

### Azure AKS - Minimal Setup:
| Resource | Cost (Monthly) |
|----------|----------------|
| 1x Standard_B2s node | $30 |
| Azure Container Registry (Basic) | $5 |
| NodePort (no LB) | $0 |
| Storage (5GB) | $1 |
| **Total** | **~$36** |

### Docker Hub + Local K8s:
| Resource | Cost (Monthly) |
|----------|----------------|
| Docker Hub (free tier) | $0 |
| Local Kubernetes (Minikube/Kind) | $0 |
| **Total** | **$0** |

---

## 🎓 Learning Resources

### Kubernetes:
- Official Docs: https://kubernetes.io/docs/
- Kubernetes by Example: https://kubernetesbyexample.com/
- Katacoda Interactive: https://www.katacoda.com/courses/kubernetes

### Prometheus & Grafana:
- Prometheus Docs: https://prometheus.io/docs/
- Grafana Tutorials: https://grafana.com/tutorials/
- PromQL Tutorial: https://promlabs.com/promql-cheat-sheet/

### Azure AKS:
- AKS Docs: https://learn.microsoft.com/en-us/azure/aks/
- AKS Best Practices: https://learn.microsoft.com/en-us/azure/aks/best-practices

---

## 🐛 Troubleshooting

### Common Issues:

#### 1. ImagePullBackOff
**Cause**: Cannot pull image from registry

**Solution**:
```powershell
# For Docker Hub: make images public or login
# For ACR: check integration
az aks check-acr --resource-group cnpm-food-rg --name cnpm-food-aks --acr cnpmfoodacr
```

#### 2. CrashLoopBackOff
**Cause**: Pod keeps crashing

**Solution**:
```powershell
kubectl logs <pod-name> -n cnpm-food
kubectl describe pod <pod-name> -n cnpm-food
```

#### 3. Service Not Accessible
**Cause**: Network connectivity issue

**Solution**:
```powershell
kubectl get endpoints -n cnpm-food
kubectl run -it --rm debug --image=curlimages/curl --restart=Never -n cnpm-food -- curl http://user-service:8081/actuator/health
```

#### 4. MySQL Connection Failed
**Cause**: MySQL not ready or wrong credentials

**Solution**:
```powershell
kubectl logs -l app=mysql -n cnpm-food
kubectl exec -it <mysql-pod> -n cnpm-food -- mysql -u root -p
```

---

## 🎯 Next Steps

### Phase 1: Basic Deployment ✅
- [x] Create Kubernetes manifests
- [x] Set up monitoring (Prometheus + Grafana)
- [x] Create deployment scripts
- [x] Write documentation

### Phase 2: Advanced Features (Optional)
- [ ] Configure Ingress with SSL/TLS
- [ ] Set up CI/CD pipeline
- [ ] Implement Network Policies
- [ ] Add Helm charts
- [ ] Configure log aggregation (ELK stack)

### Phase 3: Production Hardening (Optional)
- [ ] Security scanning
- [ ] Backup/Restore procedures
- [ ] Disaster recovery plan
- [ ] Performance testing
- [ ] Cost optimization

---

## 📞 Support

For issues or questions:
1. Check logs: `kubectl logs <pod-name> -n cnpm-food`
2. Check status: `kubectl get pods,svc,endpoints -n cnpm-food`
3. Check events: `kubectl get events -n cnpm-food --sort-by='.lastTimestamp'`
4. Read documentation: `k8s/README.md` and `k8s/QUICKSTART.md`

---

## ✅ Checklist Before Deploy

- [ ] Docker images built
- [ ] Images pushed to registry
- [ ] Kubectl configured
- [ ] Namespace created
- [ ] MySQL deployed and ready
- [ ] All services deployed
- [ ] Monitoring installed
- [ ] Health checks passing
- [ ] Frontend accessible
- [ ] Grafana dashboards configured

---

**Status**: Ready for Deployment 🚀
**Last Updated**: 2025-11-07
**Version**: 1.0.0
