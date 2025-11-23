# Hướng dẫn Deploy Database lên Railway

## Bước 1: Tạo tài khoản Railway

1. Truy cập https://railway.app
2. Đăng ký bằng GitHub account
3. Xác nhận email

## Bước 2: Tạo MySQL Database trên Railway

### Tạo Project mới
1. Click **"New Project"**
2. Chọn **"Provision MySQL"**
3. Đặt tên project: `cnpm-food-db`

### Lấy thông tin kết nối
Sau khi tạo xong, Railway sẽ cung cấp connection details:
- **Host**: `containers-us-west-xxx.railway.app`
- **Port**: `6379` (hoặc tương tự)
- **Username**: `root`
- **Password**: `xxxx`
- **Database Name**: `railway`

## Bước 3: Kết nối và Import Database

### Option 1: Sử dụng MySQL Workbench (Khuyến nghị)

1. **Download MySQL Workbench** (nếu chưa có)
   - https://dev.mysql.com/downloads/workbench/

2. **Tạo Connection mới**
   - Mở MySQL Workbench
   - Click **"+"** bên cạnh "MySQL Connections"
   - Điền thông tin:
     - Connection Name: `Railway CNPM Food`
     - Hostname: `[RAILWAY_HOST]`
     - Port: `[RAILWAY_PORT]`
     - Username: `root`
     - Password: Click "Store in Keychain" và nhập password
   - Click **"Test Connection"** để kiểm tra
   - Click **"OK"**

3. **Import SQL File**
   - Double-click vào connection vừa tạo
   - Chọn **Server > Data Import**
   - Chọn **"Import from Self-Contained File"**
   - Browse đến file: `mysql-init/init.sql`
   - Click **"Start Import"**

### Option 2: Sử dụng MySQL CLI

```bash
# Kết nối đến Railway MySQL
mysql -h [RAILWAY_HOST] -P [RAILWAY_PORT] -u root -p

# Sau khi nhập password, import file SQL
source D:/SupremeGithub/CNPM-Food/mysql-init/init.sql
```

### Option 3: Sử dụng DBeaver (Miễn phí)

1. **Download DBeaver** 
   - https://dbeaver.io/download/

2. **Tạo Connection**
   - Click **"New Database Connection"**
   - Chọn **MySQL**
   - Điền thông tin Railway
   - Test Connection

3. **Import SQL**
   - Right-click vào connection
   - Chọn **SQL Editor > Open SQL Script**
   - Chọn file `mysql-init/init.sql`
   - Click **Execute SQL Script** (Ctrl + Alt + X)

## Bước 4: Kiểm tra Database đã tạo thành công

Chạy các lệnh SQL sau để kiểm tra:

```sql
-- Kiểm tra các databases
SHOW DATABASES;

-- Kiểm tra bảng trong user_db
USE user_db;
SHOW TABLES;
SELECT * FROM users;

-- Kiểm tra bảng trong restaurant_db
USE restaurant_db;
SHOW TABLES;
SELECT * FROM restaurants;
SELECT * FROM menu_items LIMIT 5;

-- Kiểm tra bảng trong order_db
USE order_db;
SHOW TABLES;

-- Kiểm tra bảng trong payment_db
USE payment_db;
SHOW TABLES;
```

## Bước 5: Cấu hình Connection Strings cho Render

Sau khi import thành công, lấy connection strings cho mỗi service.

### Thông tin Railway của bạn:
```
Host: caboose.proxy.rlwy.net
Port: 56379
Username: root
Password: RsXUgVBQiJNTlKlfZXCqDvbyKrqQtyHU
```

### Connection Strings cho từng service:

**User Service:**
```
SPRING_DATASOURCE_URL=jdbc:mysql://caboose.proxy.rlwy.net:56379/user_db?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=RsXUgVBQiJNTlKlfZXCqDvbyKrqQtyHU
JWT_SECRET_KEY=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
JWT_EXPIRATION=86400000
FILE_UPLOAD_DIR=/app/uploads
APP_BASE_URL=https://cnpm-food-user-service.onrender.com
APP_FRONTEND_URL=https://cnpm-food-frontend.onrender.com
```

**Restaurant Service:**
```
SPRING_DATASOURCE_URL=jdbc:mysql://caboose.proxy.rlwy.net:56379/restaurant_db?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=RsXUgVBQiJNTlKlfZXCqDvbyKrqQtyHU
```

**Order Service:**
```
SPRING_DATASOURCE_URL=jdbc:mysql://caboose.proxy.rlwy.net:56379/order_db?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=RsXUgVBQiJNTlKlfZXCqDvbyKrqQtyHU
USER_SERVICE_URL=https://cnpm-food-user-service.onrender.com
RESTAURANT_SERVICE_URL=https://cnpm-food-restaurant-service.onrender.com
```

**Payment Service:**
```
SPRING_DATASOURCE_URL=jdbc:mysql://caboose.proxy.rlwy.net:56379/payment_db?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=RsXUgVBQiJNTlKlfZXCqDvbyKrqQtyHU
ORDER_SERVICE_URL=https://cnpm-food-order-service.onrender.com
RESTAURANT_SERVICE_URL=https://cnpm-food-restaurant-service.onrender.com
```

## Bước 6: Deploy lên Render

### 1. Tạo Web Service cho Frontend

1. Truy cập https://dashboard.render.com
2. Click **"New +"** > **"Web Service"**
3. Chọn **"Deploy an existing image from a registry"**
4. Nhập: `docker.io/jelly1810/cnpm-food-frontend:latest`
5. Đặt tên: `cnpm-food-frontend`
6. Region: **Singapore** (gần Việt Nam nhất)
7. Plan: **Free**
8. Click **"Create Web Service"**

**Environment Variables:**
- Không cần env vars cho frontend (cấu hình API trong Angular)

### 2. Tạo Web Service cho API Gateway

1. New Web Service
2. Image: `docker.io/jelly1810/cnpm-food-api-gateway:latest`
3. Name: `cnpm-food-api-gateway`
4. Port: `9000`

**Environment Variables:**
```
RESTAURANT_SERVICE_URL=https://cnpm-food-restaurant-service.onrender.com
ORDER_SERVICE_URL=https://cnpm-food-order-service.onrender.com
USER_SERVICE_URL=https://cnpm-food-user-service.onrender.com
PAYMENT_SERVICE_URL=https://cnpm-food-payment-service.onrender.com
```

### 3. Tạo Web Service cho User Service

1. New Web Service
2. Image: `docker.io/jelly1810/cnpm-food-user-service:latest`
3. Name: `cnpm-food-user-service`
4. Port: `8081`

**Environment Variables:**
```
SPRING_DATASOURCE_URL=jdbc:mysql://{RAILWAY_HOST}:{RAILWAY_PORT}/user_db?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD={RAILWAY_PASSWORD}
JWT_SECRET_KEY=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
JWT_EXPIRATION=86400000
FILE_UPLOAD_DIR=/app/uploads
APP_BASE_URL=https://cnpm-food-user-service.onrender.com
APP_FRONTEND_URL=https://cnpm-food-frontend.onrender.com
NOTIFICATION_SERVICE_URL=https://cnpm-food-notification-service.onrender.com/api/v1/notifications
```

### 4. Tạo Web Service cho Restaurant Service

1. New Web Service
2. Image: `docker.io/jelly1810/cnpm-food-restaurant-service:latest`
3. Name: `cnpm-food-restaurant-service`
4. Port: `8082`

**Environment Variables:**
```
SPRING_DATASOURCE_URL=jdbc:mysql://{RAILWAY_HOST}:{RAILWAY_PORT}/restaurant_db?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD={RAILWAY_PASSWORD}
```

### 5. Tạo Web Service cho Order Service

1. New Web Service
2. Image: `docker.io/jelly1810/cnpm-food-order-service:latest`
3. Name: `cnpm-food-order-service`
4. Port: `8083`

**Environment Variables:**
```
SPRING_DATASOURCE_URL=jdbc:mysql://{RAILWAY_HOST}:{RAILWAY_PORT}/order_db?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD={RAILWAY_PASSWORD}
USER_SERVICE_URL=https://cnpm-food-user-service.onrender.com
RESTAURANT_SERVICE_URL=https://cnpm-food-restaurant-service.onrender.com
NOTIFICATION_SERVICE_URL=https://cnpm-food-notification-service.onrender.com/api/v1/notifications
```

### 6. Tạo Web Service cho Payment Service

1. New Web Service
2. Image: `docker.io/jelly1810/cnpm-food-payment-service:latest`
3. Name: `cnpm-food-payment-service`
4. Port: `8085`

**Environment Variables:**
```
SPRING_DATASOURCE_URL=jdbc:mysql://{RAILWAY_HOST}:{RAILWAY_PORT}/payment_db?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD={RAILWAY_PASSWORD}
ORDER_SERVICE_URL=https://cnpm-food-order-service.onrender.com
RESTAURANT_SERVICE_URL=https://cnpm-food-restaurant-service.onrender.com
```

## Lưu ý quan trọng

### 1. Railway Free Tier Limits
- **$5 credit/month** (khoảng 500 hours)
- Database sẽ sleep sau 5 phút không hoạt động
- Có thể upgrade lên Hobby plan ($5/month) để tránh sleep

### 2. Render Free Tier Limits
- Web services sleep sau 15 phút không hoạt động
- 750 hours/month cho free tier
- Cold start có thể mất 30-60 giây
- Nên upgrade lên Starter plan ($7/month) cho production

### 3. Thứ tự Deploy
Deploy theo thứ tự này để tránh lỗi:
1. Database (Railway) ✅
2. User Service
3. Restaurant Service
4. Order Service
5. Payment Service
6. API Gateway
7. Frontend

### 4. Kiểm tra Health
Sau khi deploy, kiểm tra:
- Frontend: `https://cnpm-food-frontend.onrender.com`
- API Gateway: `https://cnpm-food-api-gateway.onrender.com`
- User Service: `https://cnpm-food-user-service.onrender.com/actuator/health`

## Troubleshooting

### Lỗi kết nối Database
```
Communications link failure
```
**Giải pháp:**
- Kiểm tra Railway database có đang chạy không
- Kiểm tra connection string có đúng không
- Thêm `?allowPublicKeyRetrieval=true` vào connection string

### Service bị sleep
**Giải pháp:**
- Sử dụng UptimeRobot (https://uptimerobot.com) để ping service mỗi 5 phút
- Upgrade lên paid plan

### Frontend không kết nối được API
**Giải pháp:**
- Cập nhật `environment.prod.ts` trong frontend với URL của API Gateway
- Rebuild và push lại image

## Tài khoản mẫu để test

Sau khi deploy thành công, sử dụng các tài khoản này để test:

**Admin:**
- Username: `khoi`
- Password: `123456`

**User:**
- Username: `user1`
- Password: `111111`

**Restaurant Manager:**
- Username: `restaurant1`
- Password: `222222`
- Quản lý: Burger King (restaurant_id=1)
