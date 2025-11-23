# Checklist Deploy CNPM Food lên Production

## Phase 1: Chuẩn bị ✅

- [x] Build Docker images locally
- [x] Tag images với Docker Hub username (jelly1810)
- [x] Push images lên Docker Hub
- [x] Tạo file hướng dẫn deploy (DEPLOY_RAILWAY_GUIDE.md)

## Phase 2: Railway Database Setup

### 2.1 Tạo Railway Account
- [ ] Đăng ký tài khoản Railway (https://railway.app)
- [ ] Xác nhận email
- [ ] Kết nối GitHub account (optional)

### 2.2 Provision MySQL Database
- [ ] Tạo project mới: `cnpm-food-db`
- [ ] Provision MySQL service
- [ ] Lấy connection details:
  - Host: ________________
  - Port: ________________
  - Username: ________________
  - Password: ________________

### 2.3 Import Database Schema
Chọn một trong các phương pháp sau:

**Option A: MySQL Workbench**
- [ ] Download và cài MySQL Workbench
- [ ] Tạo connection đến Railway
- [ ] Test connection thành công
- [ ] Import file `mysql-init/init.sql`
- [ ] Verify: Kiểm tra 4 databases đã được tạo

**Option B: MySQL CLI**
- [ ] Kết nối: `mysql -h [HOST] -P [PORT] -u root -p`
- [ ] Import: `source mysql-init/init.sql`
- [ ] Verify: `SHOW DATABASES;`

**Option C: DBeaver**
- [ ] Download và cài DBeaver
- [ ] Tạo MySQL connection
- [ ] Import SQL script
- [ ] Verify databases

### 2.4 Verify Database Import
```sql
-- Chạy các lệnh sau để kiểm tra
SHOW DATABASES;  -- Phải có: user_db, order_db, restaurant_db, payment_db

USE user_db;
SELECT COUNT(*) FROM users;  -- Phải có 3 users

USE restaurant_db;
SELECT COUNT(*) FROM restaurants;  -- Phải có 10 restaurants
SELECT COUNT(*) FROM menu_items;   -- Phải có nhiều menu items
```
- [ ] user_db: _____ users
- [ ] restaurant_db: _____ restaurants, _____ menu items
- [ ] order_db: Có bảng orders, order_items
- [ ] payment_db: Có bảng payments

## Phase 3: Generate Environment Variables

- [ ] Chạy script: `.\generate-render-env.ps1`
- [ ] Nhập Railway connection details
- [ ] Verify các file .env đã được tạo trong folder `render-env/`

## Phase 4: Render Deployment

### 4.1 Tạo Render Account
- [ ] Đăng ký Render (https://dashboard.render.com)
- [ ] Xác nhận email
- [ ] Kết nối GitHub (optional)

### 4.2 Deploy User Service
- [ ] New Web Service > Existing Docker Image
- [ ] Image: `docker.io/jelly1810/cnpm-food-user-service:latest`
- [ ] Name: `cnpm-food-user-service`
- [ ] Region: Singapore
- [ ] Instance Type: Free
- [ ] Copy env vars từ `render-env/user-service.env`
- [ ] Advanced > Port: `8081`
- [ ] Click "Create Web Service"
- [ ] Đợi deploy xong (5-10 phút)
- [ ] Test health: https://cnpm-food-user-service.onrender.com/actuator/health
- [ ] Status: _______________

### 4.3 Deploy Restaurant Service
- [ ] New Web Service > Existing Docker Image
- [ ] Image: `docker.io/jelly1810/cnpm-food-restaurant-service:latest`
- [ ] Name: `cnpm-food-restaurant-service`
- [ ] Region: Singapore
- [ ] Copy env vars từ `render-env/restaurant-service.env`
- [ ] Port: `8082`
- [ ] Đợi deploy xong
- [ ] Test: https://cnpm-food-restaurant-service.onrender.com/actuator/health
- [ ] Status: _______________

### 4.4 Deploy Order Service
- [ ] New Web Service > Existing Docker Image
- [ ] Image: `docker.io/jelly1810/cnpm-food-order-service:latest`
- [ ] Name: `cnpm-food-order-service`
- [ ] Region: Singapore
- [ ] Copy env vars từ `render-env/order-service.env`
- [ ] Port: `8083`
- [ ] Đợi deploy xong
- [ ] Test: https://cnpm-food-order-service.onrender.com/actuator/health
- [ ] Status: _______________

### 4.5 Deploy Payment Service
- [ ] New Web Service > Existing Docker Image
- [ ] Image: `docker.io/jelly1810/cnpm-food-payment-service:latest`
- [ ] Name: `cnpm-food-payment-service`
- [ ] Region: Singapore
- [ ] Copy env vars từ `render-env/payment-service.env`
- [ ] Port: `8085`
- [ ] Đợi deploy xong
- [ ] Test: https://cnpm-food-payment-service.onrender.com/actuator/health
- [ ] Status: _______________

### 4.6 Deploy API Gateway
- [ ] New Web Service > Existing Docker Image
- [ ] Image: `docker.io/jelly1810/cnpm-food-api-gateway:latest`
- [ ] Name: `cnpm-food-api-gateway`
- [ ] Region: Singapore
- [ ] Copy env vars từ `render-env/api-gateway.env`
- [ ] Port: `9000`
- [ ] Đợi deploy xong
- [ ] Test: https://cnpm-food-api-gateway.onrender.com
- [ ] Status: _______________

### 4.7 Deploy Frontend
- [ ] New Web Service > Existing Docker Image
- [ ] Image: `docker.io/jelly1810/cnpm-food-frontend:latest`
- [ ] Name: `cnpm-food-frontend`
- [ ] Region: Singapore
- [ ] Port: `4200`
- [ ] **IMPORTANT**: Cập nhật API URL trong Angular
  - Option A: Rebuild với environment.prod.ts mới
  - Option B: Dùng runtime config
- [ ] Đợi deploy xong
- [ ] Test: https://cnpm-food-frontend.onrender.com
- [ ] Status: _______________

## Phase 5: Testing & Verification

### 5.1 Test Backend Services
```bash
# User Service - Get all users
curl https://cnpm-food-user-service.onrender.com/api/v1/users

# Restaurant Service - Get all restaurants
curl https://cnpm-food-restaurant-service.onrender.com/api/v1/restaurants

# Test qua API Gateway
curl https://cnpm-food-api-gateway.onrender.com/api/v1/restaurants
```
- [ ] User Service hoạt động
- [ ] Restaurant Service hoạt động
- [ ] Order Service hoạt động
- [ ] Payment Service hoạt động
- [ ] API Gateway routing đúng

### 5.2 Test Authentication
- [ ] Đăng nhập với admin: `khoi` / `123456`
- [ ] Đăng nhập với user: `user1` / `111111`
- [ ] Đăng nhập với restaurant: `restaurant1` / `222222`

### 5.3 Test Core Features
- [ ] Xem danh sách nhà hàng
- [ ] Xem menu items
- [ ] Thêm item vào giỏ hàng
- [ ] Tạo đơn hàng mới
- [ ] Xem chi tiết đơn hàng
- [ ] Cập nhật trạng thái đơn hàng (restaurant manager)
- [ ] Thanh toán đơn hàng
- [ ] Upload ảnh profile (user service)

## Phase 6: Production Readiness

### 6.1 Security Checklist
- [ ] Change JWT_SECRET_KEY trong production
- [ ] Sử dụng strong passwords cho database
- [ ] Enable HTTPS (Render tự động)
- [ ] Review CORS settings
- [ ] Check SQL injection vulnerabilities

### 6.2 Performance Optimization
- [ ] Enable caching nếu cần
- [ ] Optimize Docker image sizes
- [ ] Consider CDN cho static assets
- [ ] Monitor response times

### 6.3 Monitoring Setup
- [ ] Set up health check endpoints
- [ ] Configure UptimeRobot (https://uptimerobot.com) để ping services
- [ ] Set up error logging/tracking
- [ ] Configure email notifications

### 6.4 Backup & Recovery
- [ ] Export Railway database backup
- [ ] Document recovery procedure
- [ ] Test restore process

## Phase 7: Post-Deployment

### 7.1 Documentation
- [ ] Cập nhật README.md với production URLs
- [ ] Document API endpoints
- [ ] Create user guide
- [ ] Document troubleshooting steps

### 7.2 Share & Communicate
- [ ] Share production URL với team
- [ ] Share test accounts
- [ ] Collect feedback
- [ ] Plan for improvements

## Troubleshooting Guide

### Service không start được
1. Check logs trong Render dashboard
2. Verify environment variables
3. Test database connection
4. Check Docker image có build đúng không

### Database connection failed
1. Kiểm tra Railway database có đang chạy
2. Verify host, port, password
3. Check `allowPublicKeyRetrieval=true` trong connection string
4. Test connection từ MySQL Workbench

### Service bị sleep (Free tier)
- Render free tier sleep sau 15 phút inactive
- Railway database sleep sau 5 phút inactive
- Solution: Use UptimeRobot hoặc upgrade plan

### Frontend không connect được API
1. Kiểm tra CORS settings
2. Verify API Gateway URL trong frontend config
3. Check network tab trong browser DevTools
4. Rebuild frontend với đúng API URL

## Production URLs

Sau khi deploy xong, ghi lại các URLs:

- **Frontend**: _______________________________
- **API Gateway**: _______________________________
- **User Service**: _______________________________
- **Restaurant Service**: _______________________________
- **Order Service**: _______________________________
- **Payment Service**: _______________________________
- **Railway DB Host**: _______________________________

## Notes & Issues

_Ghi chú các vấn đề gặp phải và cách giải quyết:_

_______________________________________________
_______________________________________________
_______________________________________________
