# Cập nhật Frontend cho Production

## 🎯 Vấn đề

Frontend hiện đang trỏ đến `localhost:9000` trong file `environment.prod.ts`. 
Khi deploy lên Render, cần cập nhật để trỏ đến URL của API Gateway trên Render.

## ✅ Giải pháp

### Bước 1: Cập nhật environment.prod.ts

Mở file: `frontend/src/environments/environment.prod.ts`

**Thay đổi từ:**
```typescript
export const environment = {
    production: true,
    baseUrl: 'http://localhost:9000/api/v1'
};
```

**Thành:**
```typescript
export const environment = {
    production: true,
    baseUrl: 'https://cnpm-food-api-gateway.onrender.com/api/v1'
};
```

### Bước 2: Rebuild và Push lại Frontend Image

Sau khi sửa file, chạy các lệnh sau:

```powershell
# Build lại frontend image
cd frontend
docker build -t cnpm-food-frontend .

# Tag với Docker Hub
docker tag cnpm-food-frontend jelly1810/cnpm-food-frontend:latest

# Push lên Docker Hub
docker push jelly1810/cnpm-food-frontend:latest

# Quay lại thư mục gốc
cd ..
```

### Bước 3: Redeploy trên Render

1. Vào Render Dashboard
2. Chọn service `cnpm-food-frontend`
3. Click **"Manual Deploy"** > **"Deploy latest commit"**
4. Hoặc Render sẽ tự động pull image mới (nếu có webhook)

## 📋 Thứ tự Deploy đầy đủ

### Phase 1: Deploy Backend Services trước

1. **User Service** (Port 8081)
   - Image: `jelly1810/cnpm-food-user-service:latest`
   - Copy env vars từ file hướng dẫn
   - Đợi status: **Live** (màu xanh)

2. **Restaurant Service** (Port 8082)
   - Image: `jelly1810/cnpm-food-restaurant-service:latest`
   - Copy env vars
   - Đợi status: **Live**

3. **Order Service** (Port 8083)
   - Image: `jelly1810/cnpm-food-order-service:latest`
   - Copy env vars
   - Đợi status: **Live**

4. **Payment Service** (Port 8085)
   - Image: `jelly1810/cnpm-food-payment-service:latest`
   - Copy env vars
   - Đợi status: **Live**

5. **API Gateway** (Port 9000)
   - Image: `jelly1810/cnpm-food-api-gateway:latest`
   - Environment Variables:
   ```
   RESTAURANT_SERVICE_URL=https://cnpm-food-restaurant-service.onrender.com
   ORDER_SERVICE_URL=https://cnpm-food-order-service.onrender.com
   USER_SERVICE_URL=https://cnpm-food-user-service.onrender.com
   PAYMENT_SERVICE_URL=https://cnpm-food-payment-service.onrender.com
   ```
   - Đợi status: **Live**

### Phase 2: Cập nhật và Deploy Frontend

6. **Cập nhật Frontend** (sau khi API Gateway đã live)
   - Lấy URL của API Gateway: `https://cnpm-food-api-gateway.onrender.com`
   - Cập nhật `environment.prod.ts`
   - Rebuild và push image mới
   - Deploy lên Render

7. **Frontend Service** (Port 4200)
   - Image: `jelly1810/cnpm-food-frontend:latest` (version mới)
   - Region: Singapore
   - Không cần env vars
   - Đợi status: **Live**

## 🔍 Kiểm tra sau khi Deploy

### 1. Test Backend Services

```bash
# Test User Service
curl https://cnpm-food-user-service.onrender.com/actuator/health

# Test Restaurant Service
curl https://cnpm-food-restaurant-service.onrender.com/actuator/health

# Test Order Service
curl https://cnpm-food-order-service.onrender.com/actuator/health

# Test Payment Service
curl https://cnpm-food-payment-service.onrender.com/actuator/health

# Test API Gateway
curl https://cnpm-food-api-gateway.onrender.com/api/v1/restaurants
```

### 2. Test Frontend

1. Mở browser: `https://cnpm-food-frontend.onrender.com`
2. Mở DevTools (F12) > Network tab
3. Kiểm tra các API calls có đang gọi đến:
   - `https://cnpm-food-api-gateway.onrender.com/api/v1/...`
   - Không phải `localhost`

### 3. Test Login

Đăng nhập với tài khoản:
- Username: `khoi`
- Password: `123456`

## ⚠️ Lưu ý quan trọng

### CORS Configuration

Nếu frontend gặp lỗi CORS, cần cập nhật backend:

Trong mỗi service Spring Boot, kiểm tra file `WebConfig.java` hoặc tương tự:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:4200",
                    "https://cnpm-food-frontend.onrender.com"  // Thêm dòng này
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

Sau khi sửa, cần rebuild và push lại backend images.

### Free Tier Sleep Mode

- **Render**: Services sleep sau 15 phút không hoạt động
- **Railway DB**: Sleep sau 5 phút không hoạt động
- **Cold start**: 30-60 giây để wake up

**Giải pháp:**
1. Dùng **UptimeRobot** (https://uptimerobot.com) để ping mỗi 5 phút
2. Hoặc upgrade lên paid plan

## 🎉 Hoàn thành

Sau khi làm xong các bước trên:

✅ Backend services chạy trên Render với Railway MySQL
✅ Frontend trỏ đúng đến API Gateway production
✅ Tất cả services có thể giao tiếp với nhau
✅ Người dùng có thể truy cập app qua URL public

**Production URLs:**
- Frontend: `https://cnpm-food-frontend.onrender.com`
- API Gateway: `https://cnpm-food-api-gateway.onrender.com`
- Databases: Railway MySQL (4 databases)
