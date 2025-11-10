CREATE DATABASE user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'userservice'@'%' IDENTIFIED WITH caching_sha2_password BY 'user';
GRANT ALL PRIVILEGES ON user_db.* TO 'userservice'@'%';
GRANT ALL PRIVILEGES ON *.* TO 'userservice'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

CREATE DATABASE order_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'orderservice'@'%' IDENTIFIED WITH caching_sha2_password BY 'order';
GRANT ALL PRIVILEGES ON order_db.* TO 'orderservice'@'%';
GRANT ALL PRIVILEGES ON *.* TO 'orderservice'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

CREATE DATABASE restaurant_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'restaurantservice'@'%' IDENTIFIED WITH caching_sha2_password BY 'res';
GRANT ALL PRIVILEGES ON restaurant_db.* TO 'restaurantservice'@'%';
GRANT ALL PRIVILEGES ON *.* TO 'restaurantservice'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

CREATE DATABASE payment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'paymentservice'@'%' IDENTIFIED WITH caching_sha2_password BY 'pay';
GRANT ALL PRIVILEGES ON payment_db.* TO 'paymentservice'@'%';
GRANT ALL PRIVILEGES ON *.* TO 'paymentservice'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

USE order_db;

-- Tạo bảng 'orders' (phải tạo bảng này trước 'order_items')
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    order_date DATETIME NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    recipient_name VARCHAR(50),
    contact_email VARCHAR(50),
    shipping_address VARCHAR(255),
    contact_phone VARCHAR(20)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Tạo bảng 'order_items' (phụ thuộc vào bảng 'orders')
CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    menu_item_id INT NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE payment_db;

-- Tạo bảng 'payments'
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    amount DECIMAL(10, 2),
    method VARCHAR(30),
    status VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE restaurant_db;

-- Tạo bảng 'restaurants' (phải tạo bảng này trước 'menu_items')
CREATE TABLE restaurants (
    restaurant_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    image VARCHAR(255)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Tạo bảng 'menu_items' (phụ thuộc vào bảng 'restaurants')
CREATE TABLE menu_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    price DECIMAL(10, 2),
    stock INT,
    image_url VARCHAR(255),
    restaurant_id INT NOT NULL,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

INSERT INTO restaurants (address, image, name) VALUES
('123 Lê Văn Sỹ, Quận 3, TP.HCM', 'target/classes/uploads/burger_king.png', 'Burger King'),
('456 Nguyễn Trãi, Quận 5, TP.HCM', 'target/classes/uploads/kfc.jpeg', 'KFC'),
('789 Trần Hưng Đạo, Quận 1, TP.HCM', 'target/classes/uploads/mcdonald.jpg', 'McDonald''s'),
('101 Phan Xích Long, Phú Nhuận, TP.HCM', 'target/classes/uploads/pizza_hut.png', 'Pizza Hut'),
('202 Điện Biên Phủ, Bình Thạnh, TP.HCM', 'target/classes/uploads/domino.png', 'Domino''s Pizza'),
('303 Nguyễn Thị Minh Khai, Quận 1, TP.HCM', 'target/classes/uploads/lotteria.png', 'Lotteria'),
('404 Cách Mạng Tháng 8, Quận 10, TP.HCM', 'target/classes/uploads/jollibee.jpg', 'Jollibee'),
('505 Võ Văn Tần, Quận 3, TP.HCM', 'target/classes/uploads/subway.png', 'Subway'),
('606 Nguyễn Đình Chiểu, Quận 3, TP.HCM', 'target/classes/uploads/tocotoco.jpg', 'TocoToco'),
('707 Lý Chính Thắng, Quận 3, TP.HCM', 'target/classes/uploads/highland.png', 'Highlands Coffee');
INSERT INTO menu_items (image_url, name, price, stock, restaurant_id) VALUES
('target/classes/uploads/burger_beef_cheese.jpg', 'Burger bò phô mai', 65000, 50, 1),
('target/classes/uploads/burger_spicy_chicken.jpg', 'Burger gà cay', 60000, 40, 4),
('target/classes/uploads/burger_veggie.jpg', 'Burger chay', 55000, 30, 1),
('target/classes/uploads/french_fries.jpeg', 'Khoai tây chiên', 30000, 100, 1),
('target/classes/uploads/fried_chicken_1.jpg', 'Gà rán phần 1 miếng', 35000, 80, 2),
('target/classes/uploads/fried_chicken_2.png', 'Gà rán phần 2 miếng', 65000, 60, 5),
('target/classes/uploads/hotdog_classic.jpg', 'Hotdog truyền thống', 40000, 70, 1),
('target/classes/uploads/hotdog_cheese.jpg', 'Hotdog phô mai', 45000, 60, 1),
('target/classes/uploads/pizza_cheese.jpg', 'Pizza phô mai', 90000, 40, 6),
('target/classes/uploads/pizza_seafood.jpg', 'Pizza hải sản', 110000, 35, 3),
('target/classes/uploads/pizza_sausage.jpg', 'Pizza xúc xích', 95000, 45, 3),
('target/classes/uploads/chicken_nuggets.jpg', 'Nuggets gà', 40000, 90, 2),
('target/classes/uploads/sandwich_ham.jpg', 'Sandwich thịt nguội', 45000, 50, 1),
('target/classes/uploads/sandwich_egg.jpg', 'Sandwich trứng', 40000, 50, 1),
('target/classes/uploads/taco_beef.jpg', 'Taco bò', 50000, 40, 1),
('target/classes/uploads/taco_chicken.jpg', 'Taco gà', 50000, 40, 1),
('target/classes/uploads/salad_grilled_chicken.jpg', 'Salad gà nướng', 55000, 30, 2),
('target/classes/uploads/salad_tuna.jpg', 'Salad cá ngừ', 60000, 30, 2),
('target/classes/uploads/milkshake_chocolate.jpg', 'Sữa lắc socola', 45000, 60, 7),
('target/classes/uploads/milkshake_strawberry.jpg', 'Sữa lắc dâu', 45000, 60, 3);

Use user_db;

-- 1. Tạo bảng 'users' (phải tạo trước 'user_roles' và 'password_reset_token')
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    profile_image_name VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    restaurant_id BIGINT DEFAULT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. Tạo bảng 'user_roles' (từ @ElementCollection)
CREATE TABLE user_roles (
    user_id INT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. Tạo bảng 'password_reset_token'
CREATE TABLE password_reset_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id INT NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================
-- TÀI KHOẢN MẪU (3 TÀI KHOẢN)
-- ============================================
INSERT INTO users (address, email, is_active, password, profile_image_name, username, restaurant_id) VALUES
-- khoi - password: 123456 (ROLE_ADMIN)
('789 Admin Street', 'khoi@admin.com', 1, '$2a$10$N9qo8uLOickgx2ZMRZoMye6mJIAzN5kJiOdQ5Xl8JJl8UvQQ5hWoW', NULL, 'khoi', NULL),
-- user1 - password: 111111 (ROLE_USER)
('456 User Street', 'user1@example.com', 1, '$2a$12$46jwxBV9b1rtqQaiWmSr9eXITEvpIQ71pSrSDOlVDRfTfjHL17q.6', NULL, 'user1', NULL),
-- restaurant1 - password: 222222 (ROLE_RESTAURANT, quản lý Burger King - restaurant_id=1)
('789 Restaurant Street', 'restaurant1@example.com', 1, '$2a$12$oN9b50keeSv82nCYuj1LueB96WSUOa2LXz9XnvUH.tK8L/NgOSFRi', NULL, 'restaurant1', 1);

INSERT INTO user_roles (user_id, role)
SELECT user_id, 'ROLE_ADMIN'
FROM users
WHERE username = 'khoi';

INSERT INTO user_roles (user_id, role)
SELECT user_id, 'ROLE_USER'
FROM users
WHERE username = 'user1';

INSERT INTO user_roles (user_id, role)
SELECT user_id, 'ROLE_RESTAURANT'
FROM users
WHERE username = 'restaurant1';

