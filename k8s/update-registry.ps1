# Helper Script - Update Docker Registry in K8s Files
# This script updates all Kubernetes deployment files with your Docker registry

param(
    [Parameter(Mandatory=$true)]
    [string]$Registry,
    
    [Parameter(Mandatory=$false)]
    [string]$OldRegistry = "cnpmfoodacr.azurecr.io"
)

Write-Host "🔄 Updating Docker Registry in Kubernetes files..." -ForegroundColor Cyan
Write-Host "From: $OldRegistry" -ForegroundColor Yellow
Write-Host "To:   $Registry" -ForegroundColor Green
Write-Host ""

$deploymentFiles = Get-ChildItem -Path "k8s/services" -Filter "*-deployment.yaml" -Exclude "mysql-deployment.yaml"

$updatedCount = 0

foreach ($file in $deploymentFiles) {
    Write-Host "Processing: $($file.Name)" -ForegroundColor Gray
    
    $content = Get-Content $file.FullName -Raw
    
    if ($content -match $OldRegistry) {
        $newContent = $content -replace [regex]::Escape($OldRegistry), $Registry
        Set-Content -Path $file.FullName -Value $newContent -NoNewline
        Write-Host "  ✅ Updated" -ForegroundColor Green
        $updatedCount++
    } else {
        Write-Host "  ⏭️  Skipped (no changes needed)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "✅ Updated $updatedCount files successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Build images: docker-compose build" -ForegroundColor White
Write-Host "2. Tag images:   docker tag cnpm-food-user-service $Registry/user-service:latest" -ForegroundColor White
Write-Host "3. Push images:  docker push $Registry/user-service:latest" -ForegroundColor White
Write-Host "4. Deploy:       kubectl apply -f k8s/services/" -ForegroundColor White
