# Build All Docker Images for CNPM Food
# This script builds all Docker images at once

param(
    [Parameter(Mandatory=$true)]
    [string]$Registry,
    
    [switch]$Push,
    [switch]$NoBuild
)

$ErrorActionPreference = "Stop"

$services = @(
    "user-service",
    "restaurant-service", 
    "order-service",
    "payment-service",
    "api-gateway",
    "frontend"
)

Write-Host "🐳 Docker Build Script for CNPM Food" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Registry: $Registry" -ForegroundColor Green
Write-Host "Push: $Push" -ForegroundColor Yellow
Write-Host ""

$totalServices = $services.Count
$currentService = 0
$successCount = 0
$failedServices = @()

foreach ($service in $services) {
    $currentService++
    Write-Host "[$currentService/$totalServices] 🔨 Processing: $service" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
    
    $imageName = "$Registry/${service}:latest"
    
    try {
        # Build
        if (-not $NoBuild) {
            Write-Host "  Building image..." -ForegroundColor Yellow
            docker build -t $imageName "./$service" --quiet
            if ($LASTEXITCODE -ne 0) {
                throw "Build failed"
            }
            Write-Host "  ✅ Build successful" -ForegroundColor Green
        } else {
            Write-Host "  ⏭️  Skipping build (--NoBuild)" -ForegroundColor Yellow
        }
        
        # Push
        if ($Push) {
            Write-Host "  Pushing to registry..." -ForegroundColor Yellow
            docker push $imageName --quiet
            if ($LASTEXITCODE -ne 0) {
                throw "Push failed"
            }
            Write-Host "  ✅ Push successful" -ForegroundColor Green
        }
        
        $successCount++
        Write-Host ""
    }
    catch {
        Write-Host "  ❌ Failed: $_" -ForegroundColor Red
        $failedServices += $service
        Write-Host ""
    }
}

# Summary
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "📊 Summary" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "Total services:    $totalServices" -ForegroundColor White
Write-Host "Successful:        $successCount" -ForegroundColor Green
Write-Host "Failed:            $($failedServices.Count)" -ForegroundColor Red

if ($failedServices.Count -gt 0) {
    Write-Host ""
    Write-Host "Failed services:" -ForegroundColor Red
    foreach ($service in $failedServices) {
        Write-Host "  - $service" -ForegroundColor Red
    }
    exit 1
}

Write-Host ""
Write-Host "✅ All images built successfully!" -ForegroundColor Green

if ($Push) {
    Write-Host "✅ All images pushed successfully!" -ForegroundColor Green
}

Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  kubectl apply -f k8s/services/mysql-deployment.yaml" -ForegroundColor White
Write-Host "  kubectl wait --for=condition=ready pod -l app=mysql -n cnpm-food --timeout=300s" -ForegroundColor White
Write-Host "  kubectl apply -f k8s/services/" -ForegroundColor White
Write-Host ""

# Display image list
Write-Host "Built images:" -ForegroundColor Cyan
docker images | Select-String $Registry
