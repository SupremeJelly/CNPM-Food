#!/usr/bin/env pwsh
# 🚀 Deploy CNPM Food Application to Kubernetes
# Optimized for Docker Desktop with limited resources

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🚀 CNPM Food - K8s Deployment Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if kubectl is available
if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Host "❌ kubectl not found. Please install kubectl first." -ForegroundColor Red
    exit 1
}

# Check current context
Write-Host "📌 Current Kubernetes context:" -ForegroundColor Yellow
kubectl config current-context
Write-Host ""

# Ask for confirmation
$confirmation = Read-Host "Continue with deployment? (y/n)"
if ($confirmation -ne 'y') {
    Write-Host "❌ Deployment cancelled." -ForegroundColor Red
    exit 0
}

# Function to wait for deployment
function Wait-ForDeployment {
    param(
        [string]$Name,
        [string]$Namespace = "cnpm-food",
        [int]$TimeoutSeconds = 300
    )
    
    Write-Host "⏳ Waiting for $Name to be ready..." -ForegroundColor Yellow
    kubectl rollout status deployment/$Name -n $Namespace --timeout="${TimeoutSeconds}s"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ $Name is ready!" -ForegroundColor Green
    } else {
        Write-Host "❌ $Name failed to start. Check logs:" -ForegroundColor Red
        Write-Host "   kubectl logs deployment/$Name -n $Namespace --tail=50" -ForegroundColor Yellow
    }
}

# Function to check pod status
function Show-PodStatus {
    param([string]$Namespace = "cnpm-food")
    
    Write-Host ""
    Write-Host "📊 Pod Status:" -ForegroundColor Cyan
    kubectl get pods -n $Namespace -o wide
    Write-Host ""
}

# Step 1: Create namespace
Write-Host ""
Write-Host "1️⃣ Creating namespace..." -ForegroundColor Cyan
kubectl create namespace cnpm-food --dry-run=client -o yaml | kubectl apply -f -

# Step 2: Deploy MySQL
Write-Host ""
Write-Host "2️⃣ Deploying MySQL..." -ForegroundColor Cyan
kubectl apply -f k8s/services/mysql-deployment.yaml

Write-Host "⏳ Waiting for MySQL to be ready (this may take 1-2 minutes)..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=mysql -n cnpm-food --timeout=300s

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ MySQL is ready!" -ForegroundColor Green
} else {
    Write-Host "❌ MySQL failed to start. Aborting deployment." -ForegroundColor Red
    Show-PodStatus
    exit 1
}

# Step 3: Deploy backend services
Write-Host ""
Write-Host "3️⃣ Deploying backend services..." -ForegroundColor Cyan
kubectl apply -f k8s/services/user-service-deployment.yaml
kubectl apply -f k8s/services/restaurant-service-deployment.yaml
kubectl apply -f k8s/services/order-service-deployment.yaml
kubectl apply -f k8s/services/payment-service-deployment.yaml

Write-Host "⏳ Waiting for backend services (this may take 2-3 minutes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 10  # Give pods time to start

Wait-ForDeployment -Name "user-service"
Wait-ForDeployment -Name "restaurant-service"
Wait-ForDeployment -Name "order-service"
Wait-ForDeployment -Name "payment-service"

# Step 4: Deploy API Gateway
Write-Host ""
Write-Host "4️⃣ Deploying API Gateway..." -ForegroundColor Cyan
kubectl apply -f k8s/services/api-gateway-deployment.yaml
Wait-ForDeployment -Name "api-gateway"

# Step 5: Deploy Frontend
Write-Host ""
Write-Host "5️⃣ Deploying Frontend..." -ForegroundColor Cyan
kubectl apply -f k8s/services/frontend-deployment.yaml
Wait-ForDeployment -Name "frontend" -TimeoutSeconds 180

# Step 6: Show deployment summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📊 Deployment Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Show-PodStatus

Write-Host "📡 Services:" -ForegroundColor Cyan
kubectl get svc -n cnpm-food
Write-Host ""

# Step 7: Get access URLs
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🌐 Access URLs" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$frontendPort = kubectl get svc frontend -n cnpm-food -o jsonpath='{.spec.ports[0].nodePort}' 2>$null
$gatewayPort = kubectl get svc api-gateway -n cnpm-food -o jsonpath='{.spec.ports[0].nodePort}' 2>$null

if ($frontendPort) {
    Write-Host "🌍 Frontend: http://localhost:$frontendPort" -ForegroundColor Green
} else {
    Write-Host "🌍 Frontend: http://localhost (LoadBalancer service)" -ForegroundColor Green
}

if ($gatewayPort) {
    Write-Host "🔌 API Gateway: http://localhost:$gatewayPort" -ForegroundColor Green
} else {
    Write-Host "🔌 API Gateway: http://localhost:9000 (LoadBalancer service)" -ForegroundColor Green
}

Write-Host ""

# Step 8: Optional - Deploy monitoring
Write-Host "========================================" -ForegroundColor Cyan
$deployMonitoring = Read-Host "Deploy monitoring stack (Prometheus + Grafana)? (y/n)"

if ($deployMonitoring -eq 'y') {
    Write-Host ""
    Write-Host "6️⃣ Deploying monitoring stack..." -ForegroundColor Cyan
    kubectl apply -f k8s/monitoring/prometheus-deployment.yaml
    kubectl apply -f k8s/monitoring/grafana-deployment.yaml
    
    Write-Host "⏳ Waiting for monitoring services..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
    
    Write-Host ""
    Write-Host "📊 Monitoring URLs:" -ForegroundColor Cyan
    Write-Host "📈 Prometheus: http://localhost:30090" -ForegroundColor Green
    Write-Host "📊 Grafana: http://localhost:30300 (admin/admin)" -ForegroundColor Green
}

# Step 9: Resource usage
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "💻 Resource Usage" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Check if metrics-server is available
$metricsAvailable = kubectl top nodes 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Node Resources:" -ForegroundColor Yellow
    kubectl top nodes
    
    Write-Host ""
    Write-Host "Pod Resources:" -ForegroundColor Yellow
    kubectl top pods -n cnpm-food
} else {
    Write-Host "⚠️ Metrics server not available. Install with:" -ForegroundColor Yellow
    Write-Host "kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ Deployment Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📝 Next Steps:" -ForegroundColor Yellow
Write-Host "1. Wait for all pods to be Running (1/1)" -ForegroundColor White
Write-Host "2. Access frontend at http://localhost" -ForegroundColor White
Write-Host "3. Check logs if any service fails:" -ForegroundColor White
Write-Host "   kubectl logs -f deployment/<service-name> -n cnpm-food" -ForegroundColor Gray
Write-Host ""
Write-Host "🔍 Useful Commands:" -ForegroundColor Yellow
Write-Host "kubectl get pods -n cnpm-food        # Check pod status" -ForegroundColor Gray
Write-Host "kubectl get svc -n cnpm-food         # Check services" -ForegroundColor Gray
Write-Host "kubectl logs -f deployment/<name> -n cnpm-food  # View logs" -ForegroundColor Gray
Write-Host "kubectl describe pod <name> -n cnpm-food        # Debug pod issues" -ForegroundColor Gray
Write-Host ""
