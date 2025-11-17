# 🖼️ Hướng dẫn sửa lỗi hình ảnh và font chữ

## 📋 Vấn đề đã được khắc phục:

### 1. **Hình ảnh món ăn và nhà hàng không hiển thị**
   - **Nguyên nhân**: Đường dẫn `src/main/resources/uploads` chỉ tồn tại lúc dev, không có trong container
   - **Giải pháp**: 
     - ✅ Copy tất cả hình ảnh vào `/app/uploads` trong Dockerfile
     - ✅ Sử dụng environment variable `UPLOAD_DIR=/app/uploads`
     - ✅ Bật endpoint `/api/v1/menu-items/images/{filename}` trong `MenuItemController`

### 2. **Font chữ tiếng Việt**
   - Font đang load từ Google Fonts CDN (Noto Sans, Roboto)
   - Nếu vẫn bị lỗi, kiểm tra Content Security Policy hoặc network

## 🔧 Các thay đổi đã thực hiện:

### **Backend (restaurant-service)**

#### 1. `Dockerfile` - Copy hình ảnh vào container
```dockerfile
# Tạo thư mục uploads với quyền ghi
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app/uploads

# Copy hình ảnh có sẵn từ source code
COPY --from=build /workspace/src/main/resources/uploads/* /app/uploads/
```

#### 2. `application.properties` - Sử dụng env variable
```properties
# Dùng /app/uploads trong production, fallback cho local dev
file.upload-dir=${UPLOAD_DIR:/app/uploads}
```

#### 3. `MenuItemController.java` - Bật endpoint serve hình ảnh
```java
@GetMapping("/images/{filename:.+}")
public ResponseEntity<Resource> getImage(@PathVariable String filename) {
    Resource resource = imageService.getImage(filename);
    return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
            .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000")
            .body(resource);
}
```

#### 4. `restaurant-service-deployment.yaml` - Set environment variable
```yaml
env:
- name: UPLOAD_DIR
  value: "/app/uploads"
```

### **Luồng hoạt động:**

```
Frontend                API Gateway             Restaurant Service
   |                         |                          |
   |  GET /api/v1/           |                          |
   |  restaurants            |                          |
   |------------------------>|                          |
   |                         |  GET /restaurants        |
   |                         |------------------------->|
   |                         |                          |
   |                         |  Response:               |
   |                         |  {                       |
   |                         |    imageUrl: "/api/v1/   |
   |                         |    restaurants/images/   |
   |                         |    burger_king.png"      |
   |                         |  }                       |
   |<------------------------|<-------------------------|
   |                         |                          |
   |  GET /api/v1/           |                          |
   |  restaurants/images/    |                          |
   |  burger_king.png        |                          |
   |------------------------>|                          |
   |                         |  GET /restaurants/       |
   |                         |  images/burger_king.png  |
   |                         |------------------------->|
   |                         |                          |
   |                         |  Read from /app/uploads/ |
   |                         |  burger_king.png         |
   |                         |                          |
   |  <-- Image Binary ------|<-------------------------|
```

## 🚀 Cách deploy:

### Bước 1: Build lại restaurant-service
```bash
cd restaurant-service
mvn clean package -DskipTests
```

### Bước 2: Build và push Docker image
```bash
docker build -t onlykohi/cnpm-restaurant-service:latest .
docker push onlykohi/cnpm-restaurant-service:latest
```

### Bước 3: Deploy lên Kubernetes
```bash
kubectl delete pod -l app=restaurant-service -n cnpm-food
kubectl apply -f k8s/services/restaurant-service-deployment.yaml
```

### Bước 4: Kiểm tra logs
```bash
kubectl logs -f deployment/restaurant-service -n cnpm-food
```

## 🧪 Test hình ảnh:

### Test qua API Gateway (port 9000):
```bash
# Test restaurant image
curl http://localhost:9000/api/v1/restaurants/images/burger_king.png -o test.png

# Test menu item image  
curl http://localhost:9000/api/v1/menu-items/images/burger_beef_cheese.jpg -o test.jpg
```

### Test trong browser:
```
http://localhost:9000/api/v1/restaurants/images/burger_king.png
http://localhost:9000/api/v1/menu-items/images/pizza_cheese.jpg
```

## 📸 Hình ảnh có sẵn trong `/app/uploads`:

### Restaurant images:
- burger_king.png
- kfc.jpeg
- mcdonald.jpg
- pizza_hut.png
- domino.png
- lotteria.png
- jollibee.jpg
- subway.png
- tocotoco.jpg
- highland.png

### Menu item images:
- burger_beef_cheese.jpg
- burger_spicy_chicken.jpg
- burger_veggie.jpg
- french_fries.jpeg
- fried_chicken_1.jpg
- fried_chicken_2.png
- hotdog_classic.jpg
- hotdog_cheese.jpg
- pizza_cheese.jpg
- pizza_seafood.jpg
- pizza_sausage.jpg
- chicken_nuggets.jpg
- sandwich_ham.jpg
- sandwich_egg.jpg
- taco_beef.jpg
- taco_chicken.jpg
- salad_grilled_chicken.jpg
- salad_tuna.jpg
- milkshake_chocolate.jpg
- milkshake_strawberry.jpg

## 🔍 Troubleshooting:

### 1. Hình ảnh vẫn không hiển thị:
```bash
# Kiểm tra xem file có trong container không
kubectl exec -it deployment/restaurant-service -n cnpm-food -- ls -la /app/uploads

# Kiểm tra quyền file
kubectl exec -it deployment/restaurant-service -n cnpm-food -- ls -la /app/uploads | head -5
```

### 2. Lỗi 404 Not Found:
- Kiểm tra API Gateway có route đúng không
- Kiểm tra endpoint `/api/v1/menu-items/images/` đã được uncomment
- Xem logs: `kubectl logs -f deployment/restaurant-service -n cnpm-food`

### 3. Font chữ không load:
- Kiểm tra network trong browser DevTools
- Xem có bị block bởi CSP không
- Thử load trực tiếp: https://fonts.googleapis.com/css2?family=Noto+Sans

### 4. Hình ảnh upload mới không lưu được:
- Container restart sẽ mất data
- Cần dùng PersistentVolume cho production:
```yaml
volumes:
- name: uploads
  persistentVolumeClaim:
    claimName: restaurant-uploads-pvc
```

## ✅ Checklist deploy:

- [x] Sửa Dockerfile copy hình ảnh
- [x] Set UPLOAD_DIR environment variable
- [x] Uncomment endpoint `/api/v1/menu-items/images/`
- [x] Build và push Docker image mới
- [x] Delete pod để force pull image mới
- [x] Test hình ảnh qua browser/curl

## 📝 Lưu ý quan trọng:

1. **Hình ảnh trong container là READ-ONLY sau khi build**
   - Upload mới sẽ bị mất khi pod restart
   - Production cần PersistentVolume

2. **Cache 1 năm cho hình ảnh**
   - Header: `Cache-Control: max-age=31536000`
   - Nếu sửa hình, đổi tên file để bypass cache

3. **Content-Type tự động**
   - Hiện tại hardcode `MediaType.IMAGE_JPEG`
   - Nên detect dựa trên file extension (.png, .jpg, .jpeg)

4. **API Gateway cần route `/api/v1/restaurants/images/**` và `/api/v1/menu-items/images/**`**
   - Kiểm tra Spring Cloud Gateway routes

---

**Tác giả**: Senior DevOps Engineer  
**Ngày tạo**: 17/11/2025  
**Version**: 1.0
