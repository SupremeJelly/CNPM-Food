# 🚀 START HERE - Quick Deployment Guide

> **For users who pulled this project from GitHub**

## ⚡ Quick Start (3 Steps)

### **Step 1: Configure Docker Desktop**

1. Open **Docker Desktop**
2. Go to **Settings** → **Resources**
3. Set the following:
   - **Memory**: `4GB` (minimum) or `6GB` (recommended)
   - **CPUs**: `2` (minimum) or `4` (recommended)
   - **Swap**: `1GB`
4. Go to **Settings** → **Kubernetes**
5. Check ✅ **Enable Kubernetes**
6. Click **Apply & Restart**

---

### **Step 2: Deploy Application**

Open PowerShell in project directory:

```powershell
cd CNPM-Food
./k8s/deploy-optimized.ps1
```

**That's it!** 🎉

The script will:
- ✅ Create namespace
- ✅ Deploy MySQL
- ✅ Deploy all microservices
- ✅ Deploy API Gateway
- ✅ Deploy Frontend
- ✅ Show access URLs

---

### **Step 3: Access Application**

After deployment completes (2-3 minutes):

- 🌍 **Frontend**: http://localhost
- 🔌 **API Gateway**: http://localhost:9000

---

## 🐛 Troubleshooting

### **Problem: Pods stuck in "Pending" or "OOMKilled"**

**Solution**: Increase Docker Desktop memory

```
Docker Desktop → Settings → Resources → Memory: 6GB
```

Then restart deployment:

```powershell
./k8s/cleanup.ps1
./k8s/deploy-optimized.ps1
```

---

### **Problem: "ImagePullBackOff" error**

**Solution**: Images are already on Docker Hub (public), but if you need to build locally:

```powershell
# Build all images
docker build -t onlykohi/cnpm-user-service:latest ./user-service
docker build -t onlykohi/cnpm-order-service:latest ./order-service
docker build -t onlykohi/cnpm-restaurant-service:latest ./restaurant-service
docker build -t onlykohi/cnpm-payment-service:latest ./payment-service
docker build -t onlykohi/cnpm-api-gateway:latest ./api-gateway
docker build -t onlykohi/cnpm-frontend:latest ./frontend
```

---

### **Problem: Frontend shows blank page**

**Solution**: Wait for all services to be ready

```powershell
# Check pod status (all should be 1/1 Running)
kubectl get pods -n cnpm-food

# If any pod is not Running, check logs:
kubectl logs deployment/<service-name> -n cnpm-food
```

---

## 📊 Resource Requirements

**Minimum System:**
- RAM: 4GB
- CPU: 2 cores
- Disk: 20GB free

**Recommended System:**
- RAM: 8GB
- CPU: 4 cores
- Disk: 30GB free

**Docker Desktop Settings:**
- Memory: 4-6GB
- CPUs: 2-4
- Kubernetes: Enabled

---

## 📚 Documentation

- 📖 [DEPLOYMENT_OPTIMIZATION_SUMMARY.md](./DEPLOYMENT_OPTIMIZATION_SUMMARY.md) - Full optimization details
- 📖 [k8s/RESOURCE_REQUIREMENTS.md](./k8s/RESOURCE_REQUIREMENTS.md) - Resource guide
- 📖 [k8s/QUICKSTART.md](./k8s/QUICKSTART.md) - Detailed deployment guide
- 📖 [.github/workflows/README.md](./.github/workflows/README.md) - CI/CD guide

---

## 🆘 Still Having Issues?

### Check deployment status:

```powershell
# View all pods
kubectl get pods -n cnpm-food

# View pod logs
kubectl logs deployment/user-service -n cnpm-food

# View pod details
kubectl describe pod <pod-name> -n cnpm-food

# Check resource usage
kubectl top pods -n cnpm-food
```

### Common Commands:

```powershell
# Restart a service
kubectl rollout restart deployment/user-service -n cnpm-food

# Delete and redeploy
kubectl delete deployment user-service -n cnpm-food
kubectl apply -f k8s/services/user-service-deployment.yaml

# Remove everything
./k8s/cleanup.ps1
```

---

## 🎯 What Changed?

This project has been **optimized for Docker Desktop** (November 9, 2025):

### ✅ **Resource Optimization**
- Reduced memory usage: **7GB → 3.3GB** (-53%)
- Reduced CPU usage: **3.5 cores → 1.65 cores** (-53%)
- Faster startup: **5 min → 2-3 min** (-40%)

### ✅ **CI/CD Enhancement**
- Added **automated testing** before deployment
- Test all services (backend + frontend)
- Only deploy if tests pass

### ✅ **Deployment Scripts**
- `deploy-optimized.ps1` - One-click deployment
- `cleanup.ps1` - Safe cleanup
- Better error messages and logging

---

## 🎓 For Developers

### Run Tests:

```powershell
# Backend tests
cd user-service
./mvnw clean test

# Frontend tests
cd frontend
npm install
npm test
```

### Build Images:

```powershell
# Build and push to Docker Hub
docker build -t your-username/cnpm-user-service:latest ./user-service
docker push your-username/cnpm-user-service:latest
```

### Deploy with GitHub Actions:

Push to `src` or `main` branch → Auto-deploy via CI/CD

---

**Need help?** Check the documentation files above or create an issue on GitHub.

**Happy coding!** 🚀
