DỰ ÁN SPRING BOOT MICROSERVICES

Một hệ thống giao đồ ăn có khả năng mở rộng được thiết kế theo kiến trúc Spring Boot microservices, cho phép tích hợp liền mạch và hiệu suất cao.

TỔNG QUAN KIẾN TRÚC

Dự án áp dụng kiến trúc microservices, đảm bảo tính mô-đun, cách ly lỗi và khả năng mở rộng.

THÀNH PHẦN CHÍNH

1. DỊCH VỤ BACKEND

	- API Gateway (Cổng: 8080): Điểm truy cập duy nhất cho các yêu cầu, xử lý định tuyến và cân bằng tải.
	- User Service (Cổng: 8081): Quản lý xác thực người dùng, hồ sơ cá nhân và bảo mật JWT.
	- Restaurant Service (Cổng: 8082): Xử lý dữ liệu nhà hàng, thực đơn và tải lên hình ảnh.
	- Order Service (Cổng: 8083): Xử lý đơn hàng, tích hợp với người dùng/nhà hàng và theo dõi trạng thái đơn hàng.
	- Payment Service (Cổng: 8085): Xử lý thanh toán, lưu lịch sử thanh toán và cập nhật trạng thái đơn hàng.

2. GIAO DIỆN FRONTEND

	- Giao diện được xây dựng bằng Angular 17.
	- Cung cấp giao diện trực quan và xác thực dựa trên JWT.

CÔNG NGHỆ SỬ DỤNG

Backend

	 # DỰ ÁN SPRING BOOT MICROSERVICES

	Một hệ thống giao đồ ăn có khả năng mở rộng được thiết kế theo kiến trúc Spring Boot microservices, cho phép tích hợp liền mạch và hiệu suất cao.

	## Tổng quan kiến trúc

	Dự án áp dụng kiến trúc microservices, đảm bảo tính mô-đun, cách ly lỗi và khả năng mở rộng.

	## Thành phần chính

	### 1. Dịch vụ backend

	- API Gateway (Cổng: 8080): Điểm truy cập duy nhất cho các yêu cầu, xử lý định tuyến và cân bằng tải.
	- User Service (Cổng: 8081): Quản lý xác thực người dùng, hồ sơ cá nhân và bảo mật JWT.
	- Restaurant Service (Cổng: 8082): Xử lý dữ liệu nhà hàng, thực đơn và tải lên hình ảnh.
	- Order Service (Cổng: 8083): Xử lý đơn hàng, tích hợp với người dùng/nhà hàng và theo dõi trạng thái đơn hàng.
	- Payment Service (Cổng: 8085): Xử lý thanh toán, lưu lịch sử thanh toán và cập nhật trạng thái đơn hàng.

	### 2. Giao diện frontend

	- Giao diện được xây dựng bằng Angular 17.
	- Cung cấp giao diện trực quan và xác thực dựa trên JWT.

	## Công nghệ sử dụng

	### Backend

	- Java 21, Spring Boot 3.3.4, Spring Cloud 2023.0.3
	- Spring Security (JWT), MySQL, REST API, OpenAPI (Swagger)

	### Frontend

	- Angular 17, TypeScript, RxJS, TailwindCSS

	### DevOps & công cụ

	- Docker, Maven, Git

	## Yêu cầu trước khi chạy

	Trước khi chạy dự án, hãy đảm bảo đã cài đặt:

	- Java 21
	- Node.js 18+
	- MySQL 8+
	- Docker
	- Maven

	## Chạy từng dịch vụ độc lập

	Mỗi microservice có thể được chạy như một ứng dụng Spring Boot độc lập. Thực hiện các bước sau để chạy từng dịch vụ riêng biệt.

	### 1) Chuẩn bị mã nguồn

	```bash
	git clone https://github.com/Vanhuyne/food-order-microservice.git
	cd food-order-microservice
	```

	### 2) Đi tới thư mục dịch vụ

	Mỗi dịch vụ có thư mục riêng. Ví dụ:

	```bash
	cd user-service
	```

	### 3) Cấu hình application properties

	Mở `src/main/resources/application.properties` hoặc `application.yml` và cập nhật cấu hình cơ sở dữ liệu. Ví dụ với MySQL:

	```properties
	spring.datasource.url=jdbc:mysql://localhost:3306/user_service_db
	spring.datasource.username=username
	spring.datasource.password=password
	```

	### 4) Build dịch vụ

	```bash
	mvn clean install
	```

	### 5) Thiết lập frontend

	```bash
	cd frontend
	npm install
	npm start
	```

	## Chạy bằng Docker

	```bash
	docker-compose up -d
	```

	## Cấu trúc repository

	Dưới đây là cấu trúc thư mục chính của repository:

	Thư mục/tệp quan trọng và mô tả ngắn:

	- `docker-compose.yml` — Tập hợp các dịch vụ để chạy môi trường phát triển/kiểm thử bằng Docker.
	- `k8s/` — Manifests Kubernetes để triển khai các dịch vụ lên cluster (deployments, services, ingress, v.v.).
	- `mysql-init/` — Tập lệnh khởi tạo cơ sở dữ liệu MySQL (ví dụ `init.sql`).
	- `frontend/` — Ứng dụng Angular (UI); Dockerfile để build và serve bằng Nginx.
	- `api-gateway/` — Spring Cloud Gateway (định tuyến và proxy cho microservices).
	- `user-service/` — Microservice quản lý người dùng và authentication.
	- `restaurant-service/` — Microservice quản lý nhà hàng và menu.
	- `order-service/` — Microservice xử lý đơn hàng, giảm tồn kho và gửi thông báo.
	- `payment-service/` — Microservice xử lý thanh toán và cập nhật trạng thái đơn hàng.
	- `.github/workflows/` — Các workflow CI/CD (build/push image, deploy), nếu có.

	Nếu bạn muốn, mình có thể mở rộng mô tả cho từng thư mục (ví dụ files chính trong `k8s/` hoặc `frontend/`).

	Bạn muốn mình bổ sung mô tả ngắn cho từng thư mục trong phần cấu trúc không? (Mình sẽ giữ định dạng thuần Markdown và không thêm icon.)