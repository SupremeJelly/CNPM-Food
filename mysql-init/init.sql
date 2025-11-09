CREATE DATABASE user_db;
CREATE USER IF NOT EXISTS 'userservice'@'%' IDENTIFIED WITH caching_sha2_password BY 'user';
GRANT ALL PRIVILEGES ON user_db.* TO 'userservice'@'%';
GRANT ALL PRIVILEGES ON *.* TO 'userservice'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

CREATE DATABASE order_db;
CREATE USER IF NOT EXISTS 'orderservice'@'%' IDENTIFIED WITH caching_sha2_password BY 'order';
GRANT ALL PRIVILEGES ON order_db.* TO 'orderservice'@'%';
GRANT ALL PRIVILEGES ON *.* TO 'orderservice'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

CREATE DATABASE restaurant_db;
CREATE USER IF NOT EXISTS 'restaurantservice'@'%' IDENTIFIED WITH caching_sha2_password BY 'res';
GRANT ALL PRIVILEGES ON restaurant_db.* TO 'restaurantservice'@'%';
GRANT ALL PRIVILEGES ON *.* TO 'restaurantservice'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

CREATE DATABASE payment_db;
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
);

-- Tạo bảng 'order_items' (phụ thuộc vào bảng 'orders')
CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    menu_item_id INT NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

USE payment_db;

-- Tạo bảng 'payments'
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    amount DECIMAL(10, 2),
    method VARCHAR(30),
    status VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

USE restaurant_db;

-- Tạo bảng 'restaurants' (phải tạo bảng này trước 'menu_items')
CREATE TABLE restaurants (
    restaurant_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    image VARCHAR(255)
);

-- Tạo bảng 'menu_items' (phụ thuộc vào bảng 'restaurants')
CREATE TABLE menu_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    price DECIMAL(10, 2),
    stock INT,
    image_url VARCHAR(255),
    restaurant_id INT NOT NULL,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id)
);

INSERT INTO restaurants (address, image, name) VALUES
('123 Lê Văn Sỹ, Quận 3, TP.HCM', 'images/burger_king.jpg', 'Burger King'),
('456 Nguyễn Trãi, Quận 5, TP.HCM', 'images/kfc.jpg', 'KFC'),
('789 Trần Hưng Đạo, Quận 1, TP.HCM', 'images/mcdonalds.jpg', 'McDonald\'s'),
('101 Phan Xích Long, Phú Nhuận, TP.HCM', 'images/pizza_hut.jpg', 'Pizza Hut'),
('202 Điện Biên Phủ, Bình Thạnh, TP.HCM', 'images/dominos.jpg', 'Domino\'s Pizza'),
('303 Nguyễn Thị Minh Khai, Quận 1, TP.HCM', 'images/lotteria.jpg', 'Lotteria'),
('404 Cách Mạng Tháng 8, Quận 10, TP.HCM', 'images/jollibee.jpg', 'Jollibee'),
('505 Võ Văn Tần, Quận 3, TP.HCM', 'images/subway.jpg', 'Subway'),
('606 Nguyễn Đình Chiểu, Quận 3, TP.HCM', 'images/tocotoco.jpg', 'TocoToco'),
('707 Lý Chính Thắng, Quận 3, TP.HCM', 'images/highlands.jpg', 'Highlands Coffee');

INSERT INTO menu_items (image_url, name, price, stock, restaurant_id) VALUES
('images/burger_beef_cheese.jpg', 'Burger bò phô mai', 65000, 50, 1),
('images/burger_spicy_chicken.jpg', 'Burger gà cay', 60000, 40, 4),
('images/burger_veggie.jpg', 'Burger chay', 55000, 30, 1),
('images/french_fries.jpg', 'Khoai tây chiên', 30000, 100, 1),
('images/fried_chicken_1.jpg', 'Gà rán phần 1 miếng', 35000, 80, 2),
('images/fried_chicken_2.jpg', 'Gà rán phần 2 miếng', 65000, 60, 5),
('images/hotdog_classic.jpg', 'Hotdog truyền thống', 40000, 70, 1),
('images/hotdog_cheese.jpg', 'Hotdog phô mai', 45000, 60, 1),
('images/pizza_cheese.jpg', 'Pizza phô mai', 90000, 40, 6),
('images/pizza_seafood.jpg', 'Pizza hải sản', 110000, 35, 3),
('images/pizza_sausage.jpg', 'Pizza xúc xích', 95000, 45, 3),
('images/chicken_nuggets.jpg', 'Nuggets gà', 40000, 90, 2),
('images/sandwich_ham.jpg', 'Sandwich thịt nguội', 45000, 50, 1),
('images/sandwich_egg.jpg', 'Sandwich trứng', 40000, 50, 1),
('images/taco_beef.jpg', 'Taco bò', 50000, 40, 1),
('images/taco_chicken.jpg', 'Taco gà', 50000, 40, 1),
('images/salad_grilled_chicken.jpg', 'Salad gà nướng', 55000, 30, 2),
('images/salad_tuna.jpg', 'Salad cá ngừ', 60000, 30, 2),
('images/milkshake_chocolate.jpg', 'Sữa lắc socola', 45000, 60, 7),
('images/milkshake_strawberry.jpg', 'Sữa lắc dâu', 45000, 60, 3);

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
);

-- 2. Tạo bảng 'user_roles' (từ @ElementCollection)
CREATE TABLE user_roles (
    user_id INT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 3. Tạo bảng 'password_reset_token'
CREATE TABLE password_reset_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id INT NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ============================================
-- TÀI KHOẢN MẪU (TẤT CẢ DÙNG PASSWORD: 123456)
-- ============================================
INSERT INTO users (address, email, is_active, password, profile_image_name, username) VALUES
-- user1 - password: 123456
('123 Street A', 'user1@example.com', 1, '$2a$10$N9qo8uLOickgx2ZMRZoMye6mJIAzN5kJiOdQ5Xl8JJl8UvQQ5hWoW', NULL, 'user1'),
-- user2 - password: 123456
('123 Street B', 'user2@example.com', 1, '$2a$10$N9qo8uLOickgx2ZMRZoMye6mJIAzN5kJiOdQ5Xl8JJl8UvQQ5hWoW', NULL, 'user2'),
-- user3 - password: 123456
('123 Street C', 'user3@example.com', 1, '$2a$10$N9qo8uLOickgx2ZMRZoMye6mJIAzN5kJiOdQ5Xl8JJl8UvQQ5hWoW', NULL, 'user3'),
-- user4 - password: 123456
('123 Street D', 'user4@example.com', 1, '$2a$10$N9qo8uLOickgx2ZMRZoMye6mJIAzN5kJiOdQ5Xl8JJl8UvQQ5hWoW', NULL, 'user4'),
-- user5 - password: 123456
('123 Street E', 'user5@example.com', 1, '$2a$10$N9qo8uLOickgx2ZMRZoMye6mJIAzN5kJiOdQ5Xl8JJl8UvQQ5hWoW', NULL, 'user5'),
-- user6 - password: 123456
('123 Street F', 'user6@example.com', 1, '$2a$10$N9qo8uLOickgx2ZMRZoMye6mJIAzN5kJiOdQ5Xl8JJl8UvQQ5hWoW', NULL, 'user6'),
-- user7 - password: 123456
('123 Street G', 'user7@example.com', 1, '$2a$10$N9qo8uLOickgx2ZMRZoMye6mJIAzN5kJiOdQ5Xl8JJl8UvQQ5hWoW', NULL, 'user7'),
-- user8 - password: 123456
('123 Street H', 'user8@example.com', 1, '$2a$10$N9qo8uLOickgx2ZMRZoMye6mJIAzN5kJiOdQ5Xl8JJl8UvQQ5hWoW', NULL, 'user8'),
-- user9 - password: 123456
('123 Street I', 'user9@example.com', 1, '$2a$10$N9qo8uLOickgx2ZMRZoMye6mJIAzN5kJiOdQ5Xl8JJl8UvQQ5hWoW', NULL, 'user9'),
-- user10 - password: 123456
('123 Street J', 'user10@example.com', 1, '$2a$10$N9qo8uLOickgx2ZMRZoMye6mJIAzN5kJiOdQ5Xl8JJl8UvQQ5hWoW', NULL, 'user10'),
-- admin - password: 123456 (ROLE_ADMIN)
('123 Street Z', 'admin@example.com', 1, '$2a$10$N9qo8uLOickgx2ZMRZoMye6mJIAzN5kJiOdQ5Xl8JJl8UvQQ5hWoW', NULL, 'admin'),
-- khoi - password: 123456 (ROLE_ADMIN)
('789 Admin Street', 'khoi@admin.com', 1, '$2a$10$N9qo8uLOickgx2ZMRZoMye6mJIAzN5kJiOdQ5Xl8JJl8UvQQ5hWoW', NULL, 'khoi'),
-- restaurant1 - password: 123456 (ROLE_RESTAURANT, quản lý Burger King - restaurant_id=1)
('101 Restaurant St', 'restaurant1@example.com', 1, '$2a$10$N9qo8uLOickgx2ZMRZoMye6mJIAzN5kJiOdQ5Xl8JJl8UvQQ5hWoW', NULL, 'restaurant1');

-- Update restaurant_id cho restaurant owner
UPDATE users SET restaurant_id = 1 WHERE username = 'restaurant1';

INSERT INTO user_roles (user_id, role)
SELECT user_id, 'ROLE_USER'
FROM users
WHERE username LIKE 'user%';

INSERT INTO user_roles (user_id, role)
SELECT user_id, 'ROLE_ADMIN'
FROM users
WHERE username IN ('admin', 'khoi');
-- Add ROLE_RESTAURANT for restaurant owner
INSERT INTO user_roles (user_id, role)
SELECT user_id, 'ROLE_RESTAURANT'
FROM users
WHERE username = 'restaurant1';
