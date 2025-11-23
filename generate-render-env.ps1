# Script tạo Environment Variables cho Render Services
# Chạy script này sau khi có thông tin Railway Database

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Render Environment Variables Generator" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Nhập thông tin Railway Database
Write-Host "Nhập thông tin Railway MySQL Database:" -ForegroundColor Yellow
$RAILWAY_HOST = Read-Host "Railway Host (vd: containers-us-west-xxx.railway.app)"
$RAILWAY_PORT = Read-Host "Railway Port (vd: 3306)"
$RAILWAY_PASSWORD = Read-Host "Railway Password" -AsSecureString
$RAILWAY_PASSWORD_PLAIN = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($RAILWAY_PASSWORD))

Write-Host ""
Write-Host "Đang tạo các file environment variables..." -ForegroundColor Green
Write-Host ""

# Tạo thư mục render-env nếu chưa có
$envDir = "render-env"
if (-not (Test-Path $envDir)) {
    New-Item -ItemType Directory -Path $envDir | Out-Null
}

# 1. User Service Environment Variables
$userServiceEnv = @"
SPRING_DATASOURCE_URL=jdbc:mysql://${RAILWAY_HOST}:${RAILWAY_PORT}/user_db?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=${RAILWAY_PASSWORD_PLAIN}
JWT_SECRET_KEY=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
JWT_EXPIRATION=86400000
FILE_UPLOAD_DIR=/app/uploads
APP_BASE_URL=https://cnpm-food-user-service.onrender.com
APP_FRONTEND_URL=https://cnpm-food-frontend.onrender.com
NOTIFICATION_SERVICE_URL=https://cnpm-food-notification-service.onrender.com/api/v1/notifications
"@

$userServiceEnv | Out-File -FilePath "$envDir/user-service.env" -Encoding UTF8
Write-Host "✓ Đã tạo: $envDir/user-service.env" -ForegroundColor Green

# 2. Restaurant Service Environment Variables
$restaurantServiceEnv = @"
SPRING_DATASOURCE_URL=jdbc:mysql://${RAILWAY_HOST}:${RAILWAY_PORT}/restaurant_db?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=${RAILWAY_PASSWORD_PLAIN}
"@

$restaurantServiceEnv | Out-File -FilePath "$envDir/restaurant-service.env" -Encoding UTF8
Write-Host "✓ Đã tạo: $envDir/restaurant-service.env" -ForegroundColor Green

# 3. Order Service Environment Variables
$orderServiceEnv = @"
SPRING_DATASOURCE_URL=jdbc:mysql://${RAILWAY_HOST}:${RAILWAY_PORT}/order_db?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=${RAILWAY_PASSWORD_PLAIN}
USER_SERVICE_URL=https://cnpm-food-user-service.onrender.com
RESTAURANT_SERVICE_URL=https://cnpm-food-restaurant-service.onrender.com
NOTIFICATION_SERVICE_URL=https://cnpm-food-notification-service.onrender.com/api/v1/notifications
"@

$orderServiceEnv | Out-File -FilePath "$envDir/order-service.env" -Encoding UTF8
Write-Host "✓ Đã tạo: $envDir/order-service.env" -ForegroundColor Green

# 4. Payment Service Environment Variables
$paymentServiceEnv = @"
SPRING_DATASOURCE_URL=jdbc:mysql://${RAILWAY_HOST}:${RAILWAY_PORT}/payment_db?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=${RAILWAY_PASSWORD_PLAIN}
ORDER_SERVICE_URL=https://cnpm-food-order-service.onrender.com
RESTAURANT_SERVICE_URL=https://cnpm-food-restaurant-service.onrender.com
"@

$paymentServiceEnv | Out-File -FilePath "$envDir/payment-service.env" -Encoding UTF8
Write-Host "✓ Đã tạo: $envDir/payment-service.env" -ForegroundColor Green

# 5. API Gateway Environment Variables
$apiGatewayEnv = @"
RESTAURANT_SERVICE_URL=https://cnpm-food-restaurant-service.onrender.com
ORDER_SERVICE_URL=https://cnpm-food-order-service.onrender.com
USER_SERVICE_URL=https://cnpm-food-user-service.onrender.com
PAYMENT_SERVICE_URL=https://cnpm-food-payment-service.onrender.com
"@

$apiGatewayEnv | Out-File -FilePath "$envDir/api-gateway.env" -Encoding UTF8
Write-Host "✓ Đã tạo: $envDir/api-gateway.env" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Hoàn thành!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Các file .env đã được tạo trong thư mục: $envDir/" -ForegroundColor Yellow
Write-Host "Copy nội dung từng file và paste vào Environment Variables trong Render Dashboard" -ForegroundColor Yellow
Write-Host ""
Write-Host "Thứ tự deploy trên Render:" -ForegroundColor Cyan
Write-Host "1. user-service" -ForegroundColor White
Write-Host "2. restaurant-service" -ForegroundColor White
Write-Host "3. order-service" -ForegroundColor White
Write-Host "4. payment-service" -ForegroundColor White
Write-Host "5. api-gateway" -ForegroundColor White
Write-Host "6. frontend" -ForegroundColor White
Write-Host ""

# Tạo file tổng hợp để dễ copy
$summaryFile = @"
# CNPM Food - Render Environment Variables
# Tạo ngày: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")

========================================
USER SERVICE
========================================
$userServiceEnv

========================================
RESTAURANT SERVICE
========================================
$restaurantServiceEnv

========================================
ORDER SERVICE
========================================
$orderServiceEnv

========================================
PAYMENT SERVICE
========================================
$paymentServiceEnv

========================================
API GATEWAY
========================================
$apiGatewayEnv

========================================
DOCKER IMAGES
========================================
Frontend:             docker.io/jelly1810/cnpm-food-frontend:latest
API Gateway:          docker.io/jelly1810/cnpm-food-api-gateway:latest
User Service:         docker.io/jelly1810/cnpm-food-user-service:latest
Restaurant Service:   docker.io/jelly1810/cnpm-food-restaurant-service:latest
Order Service:        docker.io/jelly1810/cnpm-food-order-service:latest
Payment Service:      docker.io/jelly1810/cnpm-food-payment-service:latest

========================================
SERVICE PORTS
========================================
Frontend:             4200
API Gateway:          9000
User Service:         8081
Restaurant Service:   8082
Order Service:        8083
Payment Service:      8085
"@

$summaryFile | Out-File -FilePath "$envDir/ALL_ENV_VARIABLES.txt" -Encoding UTF8
Write-Host "✓ Đã tạo file tổng hợp: $envDir/ALL_ENV_VARIABLES.txt" -ForegroundColor Green
Write-Host ""
