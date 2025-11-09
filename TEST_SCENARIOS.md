# Test Scenarios for Food Delivery Application

## 1. User Authentication & Authorization 🔐

### 1.1 Registration
- [ ] Kiểm tra đăng ký với thông tin hợp lệ
- [ ] Kiểm tra validate email format
- [ ] Kiểm tra password complexity
- [ ] Kiểm tra trùng email
- [ ] Kiểm tra required fields
- [ ] Kiểm tra xác nhận password match

### 1.2 Login
- [ ] Đăng nhập thành công với credentials đúng
- [ ] Đăng nhập thất bại với email không tồn tại
- [ ] Đăng nhập thất bại với password sai
- [ ] Kiểm tra JWT token generation
- [ ] Kiểm tra session timeout
- [ ] Kiểm tra remember me functionality

### 1.3 Authorization
- [ ] Kiểm tra phân quyền ROLE_USER
- [ ] Kiểm tra phân quyền ROLE_ADMIN
- [ ] Kiểm tra access denied khi không có quyền
- [ ] Kiểm tra token expiration
- [ ] Kiểm tra token refresh

## 2. Restaurant Management 🏪

### 2.1 Restaurant Listing
- [ ] Hiển thị danh sách nhà hàng
- [ ] Phân trang hoạt động đúng
- [ ] Filter theo categories
- [ ] Search theo tên nhà hàng
- [ ] Sort theo rating/distance

### 2.2 Menu Management (Admin)
- [ ] Thêm món ăn mới
- [ ] Sửa thông tin món ăn
- [ ] Upload/thay đổi ảnh món ăn
- [ ] Xóa món ăn
- [ ] Phân loại món ăn
- [ ] Set giá tiền

### 2.3 Restaurant Details
- [ ] Hiển thị thông tin nhà hàng
- [ ] Hiển thị menu đầy đủ
- [ ] Hiển thị reviews/ratings
- [ ] Hiển thị địa chỉ/contact info

## 3. Order Processing 🛒

### 3.1 Shopping Cart
- [ ] Thêm món vào giỏ hàng
- [ ] Cập nhật số lượng
- [ ] Xóa món khỏi giỏ
- [ ] Tính tổng tiền chính xác
- [ ] Lưu giỏ hàng khi chưa checkout
- [ ] Clear giỏ hàng sau checkout

### 3.2 Checkout Process
- [ ] Validate thông tin giao hàng
- [ ] Validate thông tin liên hệ
- [ ] Tính phí shipping
- [ ] Áp dụng mã giảm giá
- [ ] Tính thuế
- [ ] Xác nhận đơn hàng

### 3.3 Payment Integration
- [ ] Thanh toán COD
- [ ] Thanh toán credit card
- [ ] Xử lý payment success
- [ ] Xử lý payment failure
- [ ] Payment status update
- [ ] Generate hóa đơn

### 3.4 Order Management
- [ ] Tạo đơn hàng mới
- [ ] Cập nhật trạng thái đơn
- [ ] Hủy đơn hàng
- [ ] Xem lịch sử đơn hàng
- [ ] Order tracking
- [ ] Gửi email confirmation

## 4. Integration Testing 🔄

### 4.1 Microservices Communication
- [ ] User Service → Order Service
- [ ] Order Service → Restaurant Service
- [ ] Order Service → Payment Service
- [ ] Order Service → Notification Service
- [ ] API Gateway routing
- [ ] Service discovery (Eureka)

### 4.2 Event Processing
- [ ] Order created event
- [ ] Payment processed event
- [ ] Notification triggered
- [ ] Kafka message queues
- [ ] Event error handling

## 5. Performance Testing 🚀

### 5.1 Load Testing
- [ ] Concurrent user access
- [ ] Multiple order processing
- [ ] Database query performance
- [ ] Service response times
- [ ] API endpoint performance

### 5.2 Security Testing
- [ ] SQL injection prevention
- [ ] XSS prevention
- [ ] CSRF protection
- [ ] API rate limiting
- [ ] Data encryption
- [ ] Secure communication

## 6. Error Handling 🐛

### 6.1 Frontend Errors
- [ ] Network error handling
- [ ] API error messages
- [ ] Form validation errors
- [ ] Loading states
- [ ] Empty states
- [ ] Error boundaries

### 6.2 Backend Errors
- [ ] Database connection errors
- [ ] Service unavailable handling
- [ ] Invalid request handling
- [ ] Transaction rollback
- [ ] Logger implementation
- [ ] Error monitoring

## 7. Browser Compatibility 🌐
- [ ] Chrome latest
- [ ] Firefox latest
- [ ] Safari latest
- [ ] Edge latest
- [ ] Mobile browsers
- [ ] Responsive design

## 8. Data Validation 📝

### 8.1 Input Validation
- [ ] Email format
- [ ] Phone numbers
- [ ] Addresses
- [ ] Payment information
- [ ] Quantities
- [ ] Price ranges

### 8.2 Business Rules
- [ ] Minimum order amount
- [ ] Delivery radius
- [ ] Operating hours
- [ ] User roles/permissions
- [ ] Order status flow
- [ ] Payment rules

## Test Environment Setup 🛠️

### Required Components
1. Backend Services:
   ```bash
   - Eureka Service (8761)
   - API Gateway (8080)
   - User Service (8081)
   - Restaurant Service (8082)
   - Order Service (8083)
   - Payment Service
   - Notification Service (8084)
   ```

2. Infrastructure:
   ```bash
   - MySQL Database
   - Kafka
   - Redis (optional for caching)
   ```

3. Frontend:
   ```bash
   - Angular dev server (4200)
   - Node.js 18+
   ```

### Test Data Requirements
1. Sample Users:
   - Regular users
   - Admin users
   - Deactivated users

2. Sample Restaurants:
   - Multiple categories
   - Various menu items
   - Different price ranges

3. Sample Orders:
   - Different statuses
   - Various payment methods
   - Multiple items

## Automated Testing Tools 🤖

1. Backend Testing:
   - JUnit 5
   - Mockito
   - TestContainers
   - REST Assured

2. Frontend Testing:
   - Jasmine
   - Karma
   - Protractor/Cypress
   - Jest

3. Performance Testing:
   - JMeter
   - Gatling
   - K6

## Continuous Integration ⚡

1. GitHub Actions workflow:
   ```yaml
   - Build
   - Unit Tests
   - Integration Tests
   - E2E Tests
   - Security Scan
   - Performance Tests
   ```

2. Quality Gates:
   - Code coverage > 80%
   - No critical security issues
   - Performance thresholds met
   - All tests passing