#!/usr/bin/env pwsh
# 🧹 Cleanup CNPM Food Application from Kubernetes
# Use this to free up resources

Write-Host "========================================" -ForegroundColor Red
Write-Host "🧹 CNPM Food - K8s Cleanup Script" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Red
Write-Host ""

Write-Host "⚠️ This will DELETE all CNPM Food resources from Kubernetes!" -ForegroundColor Yellow
Write-Host ""

$confirmation = Read-Host "Are you sure you want to continue? (yes/no)"
if ($confirmation -ne 'yes') {
    Write-Host "❌ Cleanup cancelled." -ForegroundColor Green
    exit 0
}

Write-Host ""
Write-Host "Current deployments in cnpm-food namespace:" -ForegroundColor Cyan
kubectl get deployments -n cnpm-food 2>$null

if ($LASTEXITCODE -ne 0) {
    Write-Host "✅ No deployments found in cnpm-food namespace." -ForegroundColor Green
    exit 0
}

Write-Host ""
$finalConfirmation = Read-Host "Type 'DELETE' to confirm deletion"
if ($finalConfirmation -ne 'DELETE') {
    Write-Host "❌ Cleanup cancelled." -ForegroundColor Green
    exit 0
}

Write-Host ""
Write-Host "🗑️ Deleting application deployments..." -ForegroundColor Yellow

# Delete in reverse order
kubectl delete -f k8s/services/frontend-deployment.yaml --ignore-not-found=true
kubectl delete -f k8s/services/api-gateway-deployment.yaml --ignore-not-found=true
kubectl delete -f k8s/services/payment-service-deployment.yaml --ignore-not-found=true
kubectl delete -f k8s/services/order-service-deployment.yaml --ignore-not-found=true
kubectl delete -f k8s/services/restaurant-service-deployment.yaml --ignore-not-found=true
kubectl delete -f k8s/services/user-service-deployment.yaml --ignore-not-found=true
kubectl delete -f k8s/services/mysql-deployment.yaml --ignore-not-found=true

Write-Host ""
Write-Host "🗑️ Deleting monitoring stack..." -ForegroundColor Yellow
kubectl delete -f k8s/monitoring/grafana-deployment.yaml --ignore-not-found=true
kubectl delete -f k8s/monitoring/prometheus-deployment.yaml --ignore-not-found=true

Write-Host ""
$deleteNamespace = Read-Host "Delete entire cnpm-food namespace? (y/n)"
if ($deleteNamespace -eq 'y') {
    Write-Host "🗑️ Deleting namespace cnpm-food..." -ForegroundColor Yellow
    kubectl delete namespace cnpm-food
    Write-Host "✅ Namespace deleted." -ForegroundColor Green
}

$deleteMonitoringNs = Read-Host "Delete monitoring namespace? (y/n)"
if ($deleteMonitoringNs -eq 'y') {
    Write-Host "🗑️ Deleting namespace monitoring..." -ForegroundColor Yellow
    kubectl delete namespace monitoring
    Write-Host "✅ Namespace deleted." -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "✅ Cleanup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Remaining resources:" -ForegroundColor Cyan
kubectl get all -n cnpm-food 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "No resources remaining in cnpm-food namespace." -ForegroundColor Green
}
Write-Host ""
