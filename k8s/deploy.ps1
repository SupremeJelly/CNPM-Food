# CNPM Food - Kubernetes Deployment Script (PowerShell)
# This script automates the deployment of all services to Kubernetes on Windows

param(
    [string]$Registry = "cnpmfoodacr.azurecr.io",  # Change to your registry
    [string]$Namespace = "cnpm-food",
    [switch]$SkipBuild,
    [switch]$SkipPush,
    [switch]$SkipMonitoring
)

$ErrorActionPreference = "Stop"

# Colors
function Write-Step {
    param([string]$Message)
    Write-Host "[STEP] $Message" -ForegroundColor Green
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Write-Warning-Custom {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

Write-Host "🚀 CNPM Food - Kubernetes Deployment" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$Services = @("user-service", "restaurant-service", "order-service", "payment-service", "api-gateway", "frontend")

# Step 1: Check prerequisites
Write-Step "Checking prerequisites..."
$commands = @("kubectl", "docker")
foreach ($cmd in $commands) {
    if (!(Get-Command $cmd -ErrorAction SilentlyContinue)) {
        Write-Error-Custom "$cmd is not installed. Aborting."
        exit 1
    }
}
Write-Host "✓ All prerequisites installed" -ForegroundColor Green

# Step 2: Build Docker images
if (-not $SkipBuild) {
    Write-Step "Building Docker images..."
    foreach ($service in $Services) {
        Write-Step "Building $service..."
        docker build -t "$Registry/${service}:latest" "./$service"
        if ($LASTEXITCODE -ne 0) {
            Write-Error-Custom "Failed to build $service"
            exit 1
        }
    }
    Write-Host "✓ All images built successfully" -ForegroundColor Green
} else {
    Write-Warning-Custom "Skipping image build"
}

# Step 3: Push images to registry
if (-not $SkipPush) {
    $push = Read-Host "Do you want to push images to registry? (y/n)"
    if ($push -eq "y") {
        Write-Step "Pushing images to registry..."
        foreach ($service in $Services) {
            Write-Step "Pushing $service..."
            docker push "$Registry/${service}:latest"
            if ($LASTEXITCODE -ne 0) {
                Write-Error-Custom "Failed to push $service"
                exit 1
            }
        }
        Write-Host "✓ All images pushed successfully" -ForegroundColor Green
    }
} else {
    Write-Warning-Custom "Skipping image push"
}

# Step 4: Create namespace
Write-Step "Creating namespace $Namespace..."
kubectl create namespace $Namespace --dry-run=client -o yaml | kubectl apply -f -

# Step 5: Deploy MySQL
Write-Step "Deploying MySQL..."
kubectl apply -f k8s/services/mysql-deployment.yaml

Write-Step "Waiting for MySQL to be ready (max 5 minutes)..."
try {
    kubectl wait --for=condition=ready pod -l app=mysql -n $Namespace --timeout=300s
    Write-Host "✓ MySQL is ready" -ForegroundColor Green
} catch {
    Write-Error-Custom "MySQL failed to start. Checking logs..."
    kubectl logs -l app=mysql -n $Namespace --tail=50
    exit 1
}

# Step 6: Deploy microservices
Write-Step "Deploying microservices..."
$deployments = @(
    "k8s/services/user-service-deployment.yaml",
    "k8s/services/restaurant-service-deployment.yaml",
    "k8s/services/order-service-deployment.yaml",
    "k8s/services/payment-service-deployment.yaml",
    "k8s/services/api-gateway-deployment.yaml",
    "k8s/services/frontend-deployment.yaml"
)

foreach ($deployment in $deployments) {
    kubectl apply -f $deployment
}

# Step 7: Wait for services to be ready
Write-Step "Waiting for services to be ready..."
foreach ($service in $Services) {
    Write-Step "Waiting for $service..."
    try {
        kubectl wait --for=condition=ready pod -l app=$service -n $Namespace --timeout=300s
        Write-Host "✓ $service is ready" -ForegroundColor Green
    } catch {
        Write-Warning-Custom "$service failed to start. Checking logs..."
        kubectl logs -l app=$service -n $Namespace --tail=50
    }
}

# Step 8: Install monitoring
if (-not $SkipMonitoring) {
    $monitor = Read-Host "Do you want to install Prometheus + Grafana monitoring? (y/n)"
    if ($monitor -eq "y") {
        Write-Step "Installing monitoring stack..."
        
        # Add Helm repo
        helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
        helm repo update
        
        # Install kube-prometheus-stack
        helm install monitoring prometheus-community/kube-prometheus-stack `
            --namespace monitoring `
            --create-namespace `
            --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false `
            --set grafana.adminPassword=admin `
            --wait
        
        # Apply ServiceMonitors
        kubectl apply -f k8s/monitoring/servicemonitor.yaml
        
        Write-Host "✓ Monitoring installed successfully!" -ForegroundColor Green
    }
}

# Step 9: Display status
Write-Step "Deployment Status:"
Write-Host "===================="
kubectl get pods -n $Namespace
Write-Host ""
kubectl get svc -n $Namespace

# Step 10: Access information
Write-Host ""
Write-Step "Access Information:"
Write-Host "===================="
Write-Host "Frontend:       kubectl port-forward svc/frontend -n $Namespace 4200:80" -ForegroundColor Cyan
Write-Host "API Gateway:    kubectl port-forward svc/api-gateway -n $Namespace 9000:9000" -ForegroundColor Cyan
Write-Host "Grafana:        kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80" -ForegroundColor Cyan
Write-Host "Prometheus:     kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090:9090" -ForegroundColor Cyan
Write-Host ""
Write-Host "Grafana Login:  admin / admin" -ForegroundColor Yellow
Write-Host ""

# Check for external IPs
Write-Host "External IPs (if LoadBalancer):" -ForegroundColor Cyan
kubectl get svc -n $Namespace -o wide | Select-String "LoadBalancer"

Write-Host ""
Write-Host "✅ Deployment completed successfully!" -ForegroundColor Green
Write-Host "Run 'kubectl get pods -n $Namespace' to check status" -ForegroundColor Cyan
