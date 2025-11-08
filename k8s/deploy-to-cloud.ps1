# 🚀 CNPM Food - Cloud Deployment Script
# Supports: Azure AKS, Google GKE, DigitalOcean DOKS

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("azure", "gcp", "digitalocean")]
    [string]$CloudProvider,
    
    [Parameter(Mandatory=$false)]
    [string]$ClusterName = "cnpm-food-cluster",
    
    [Parameter(Mandatory=$false)]
    [string]$ResourceGroup = "cnpm-food-rg",
    
    [Parameter(Mandatory=$false)]
    [string]$Region = "eastus"
)

Write-Host "🚀 CNPM Food Cloud Deployment" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host "Cloud Provider: $CloudProvider" -ForegroundColor Yellow
Write-Host "Cluster Name: $ClusterName" -ForegroundColor Yellow
Write-Host ""

# Function to check command exists
function Test-Command {
    param($Command)
    $null = Get-Command $Command -ErrorAction SilentlyContinue
    return $?
}

# Deploy to Azure AKS
function Deploy-ToAzure {
    Write-Host "📘 Deploying to Azure AKS..." -ForegroundColor Blue
    
    # Check Azure CLI
    if (-not (Test-Command "az")) {
        Write-Host "❌ Azure CLI not found. Please install:" -ForegroundColor Red
        Write-Host "   winget install -e --id Microsoft.AzureCLI" -ForegroundColor Yellow
        exit 1
    }
    
    # Check login status
    Write-Host "Checking Azure login status..."
    $account = az account show 2>$null
    if (-not $account) {
        Write-Host "Please login to Azure..."
        az login
    }
    
    # Create resource group
    Write-Host "Creating resource group: $ResourceGroup..."
    az group create --name $ResourceGroup --location $Region
    
    # Create AKS cluster
    Write-Host "Creating AKS cluster (this may take 10-15 minutes)..."
    az aks create `
        --resource-group $ResourceGroup `
        --name $ClusterName `
        --node-count 3 `
        --node-vm-size Standard_B2s `
        --enable-addons monitoring `
        --generate-ssh-keys
    
    # Get credentials
    Write-Host "Getting AKS credentials..."
    az aks get-credentials --resource-group $ResourceGroup --name $ClusterName --overwrite-existing
    
    # Update MySQL deployment for Azure
    Write-Host "Updating MySQL deployment for Azure..."
    $mysqlFile = "services/mysql-deployment.yaml"
    (Get-Content $mysqlFile) -replace '#\s*storageClassName: managed-csi', 'storageClassName: managed-csi' | Set-Content $mysqlFile
    
    Deploy-Application
}

# Deploy to Google GKE
function Deploy-ToGCP {
    Write-Host "📕 Deploying to Google GKE..." -ForegroundColor Red
    
    # Check gcloud CLI
    if (-not (Test-Command "gcloud")) {
        Write-Host "❌ Google Cloud SDK not found. Please install from:" -ForegroundColor Red
        Write-Host "   https://cloud.google.com/sdk/docs/install" -ForegroundColor Yellow
        exit 1
    }
    
    # Get project ID
    $project = gcloud config get-value project 2>$null
    if (-not $project) {
        Write-Host "Please set your GCP project:"
        $project = Read-Host "Enter project ID"
        gcloud config set project $project
    }
    
    # Enable APIs
    Write-Host "Enabling Kubernetes Engine API..."
    gcloud services enable container.googleapis.com
    
    # Create GKE cluster
    Write-Host "Creating GKE cluster (this may take 5-10 minutes)..."
    gcloud container clusters create $ClusterName `
        --zone $Region `
        --num-nodes 3 `
        --machine-type e2-medium `
        --enable-autoscaling `
        --min-nodes 2 `
        --max-nodes 5
    
    # Get credentials
    Write-Host "Getting GKE credentials..."
    gcloud container clusters get-credentials $ClusterName --zone $Region
    
    # Update MySQL deployment for GKE
    Write-Host "Updating MySQL deployment for GKE..."
    $mysqlFile = "services/mysql-deployment.yaml"
    (Get-Content $mysqlFile) -replace 'storageClassName:.*', 'storageClassName: standard-rwo' | Set-Content $mysqlFile
    
    Deploy-Application
}

# Deploy to DigitalOcean
function Deploy-ToDigitalOcean {
    Write-Host "📗 Deploying to DigitalOcean DOKS..." -ForegroundColor Green
    
    # Check doctl
    if (-not (Test-Command "doctl")) {
        Write-Host "❌ doctl not found. Please install:" -ForegroundColor Red
        Write-Host "   choco install doctl" -ForegroundColor Yellow
        exit 1
    }
    
    # Check auth
    $auth = doctl auth list 2>$null
    if (-not $auth) {
        Write-Host "Please authenticate with DigitalOcean..."
        doctl auth init
    }
    
    # Create cluster
    Write-Host "Creating DigitalOcean Kubernetes cluster (this may take 5 minutes)..."
    doctl kubernetes cluster create $ClusterName `
        --region sgp1 `
        --size s-2vcpu-4gb `
        --count 3
    
    # Update MySQL deployment for DO
    Write-Host "Updating MySQL deployment for DigitalOcean..."
    $mysqlFile = "services/mysql-deployment.yaml"
    (Get-Content $mysqlFile) -replace 'storageClassName:.*', 'storageClassName: do-block-storage' | Set-Content $mysqlFile
    
    Deploy-Application
}

# Common deployment function
function Deploy-Application {
    Write-Host ""
    Write-Host "📦 Deploying Application..." -ForegroundColor Cyan
    Write-Host "===========================" -ForegroundColor Cyan
    
    # Verify kubectl connection
    Write-Host "Verifying cluster connection..."
    kubectl cluster-info
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Failed to connect to cluster" -ForegroundColor Red
        exit 1
    }
    
    # Create namespace
    Write-Host "Creating namespace: cnpm-food..."
    kubectl create namespace cnpm-food --dry-run=client -o yaml | kubectl apply -f -
    
    # Deploy MySQL first
    Write-Host "Deploying MySQL database..."
    kubectl apply -f services/mysql-deployment.yaml
    
    # Wait for MySQL to be ready
    Write-Host "Waiting for MySQL to be ready (timeout: 5 minutes)..."
    kubectl wait --for=condition=ready pod -l app=mysql -n cnpm-food --timeout=300s
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "⚠️  MySQL deployment timeout. Check status:" -ForegroundColor Yellow
        kubectl get pods -n cnpm-food -l app=mysql
        Write-Host "Continue deployment? (y/n): " -NoNewline
        $continue = Read-Host
        if ($continue -ne "y") {
            exit 1
        }
    }
    
    # Deploy all other services
    Write-Host "Deploying microservices..."
    kubectl apply -f services/user-service-deployment.yaml
    kubectl apply -f services/restaurant-service-deployment.yaml
    kubectl apply -f services/order-service-deployment.yaml
    kubectl apply -f services/payment-service-deployment.yaml
    kubectl apply -f services/api-gateway-deployment.yaml
    kubectl apply -f services/frontend-deployment.yaml
    
    # Wait for all pods to be ready
    Write-Host ""
    Write-Host "Waiting for all services to be ready (this may take 3-5 minutes)..."
    Start-Sleep -Seconds 120
    
    # Show deployment status
    Write-Host ""
    Write-Host "📊 Deployment Status:" -ForegroundColor Cyan
    Write-Host "=====================" -ForegroundColor Cyan
    kubectl get deployments -n cnpm-food
    
    Write-Host ""
    Write-Host "🔍 Pods Status:" -ForegroundColor Cyan
    Write-Host "===============" -ForegroundColor Cyan
    kubectl get pods -n cnpm-food
    
    Write-Host ""
    Write-Host "🌐 Services & External IPs:" -ForegroundColor Cyan
    Write-Host "===========================" -ForegroundColor Cyan
    kubectl get svc -n cnpm-food
    
    Write-Host ""
    Write-Host "✅ Deployment completed!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📝 Next Steps:" -ForegroundColor Yellow
    Write-Host "1. Wait for LoadBalancer external IPs (may take 2-5 minutes)"
    Write-Host "2. Access frontend: http://<FRONTEND-EXTERNAL-IP>"
    Write-Host "3. Access API Gateway: http://<API-GATEWAY-EXTERNAL-IP>:9000"
    Write-Host ""
    Write-Host "🔍 Useful commands:" -ForegroundColor Yellow
    Write-Host "   kubectl get pods -n cnpm-food -w      # Watch pods"
    Write-Host "   kubectl get svc -n cnpm-food          # Check external IPs"
    Write-Host "   kubectl logs <pod-name> -n cnpm-food  # View logs"
    Write-Host "   kubectl describe pod <pod-name> -n cnpm-food  # Debug issues"
}

# Main execution
switch ($CloudProvider) {
    "azure" {
        Deploy-ToAzure
    }
    "gcp" {
        Deploy-ToGCP
    }
    "digitalocean" {
        Deploy-ToDigitalOcean
    }
}

Write-Host ""
Write-Host "🎉 All done! Your application is deploying to $CloudProvider" -ForegroundColor Green
