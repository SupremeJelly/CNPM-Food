# Script to tag and push all images to Docker Hub
# Docker Hub username
$DOCKER_USERNAME = "jelly1810"

# List of services to push
$services = @(
    "frontend",
    "api-gateway",
    "user-service",
    "restaurant-service",
    "order-service",
    "payment-service"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Pushing images to Docker Hub: $DOCKER_USERNAME" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

foreach ($service in $services) {
    $localImage = "cnpm-food-$service"
    $remoteImage = "$DOCKER_USERNAME/cnpm-food-$service:latest"
    
    Write-Host "Processing: $service" -ForegroundColor Yellow
    Write-Host "  Tagging: $localImage -> $remoteImage" -ForegroundColor Gray
    
    # Tag the image
    docker tag $localImage $remoteImage
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  Tagged successfully!" -ForegroundColor Green
        Write-Host "  Pushing to Docker Hub..." -ForegroundColor Gray
        
        # Push the image
        docker push $remoteImage
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✓ $service pushed successfully!" -ForegroundColor Green
        } else {
            Write-Host "  ✗ Failed to push $service" -ForegroundColor Red
        }
    } else {
        Write-Host "  ✗ Failed to tag $service" -ForegroundColor Red
    }
    
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Docker Hub push completed!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
