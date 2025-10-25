CREATE DATABASE user_db;
CREATE USER 'userservice'@'localhost' IDENTIFIED BY 'user';
GRANT ALL PRIVILEGES ON user_db.* TO 'userservice'@'localhost';
GRANT ALL PRIVILEGES ON *.* TO 'userservice'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;

CREATE DATABASE order_db;
CREATE USER 'orderservice'@'localhost' IDENTIFIED BY 'order';
GRANT ALL PRIVILEGES ON order_db.* TO 'orderservice'@'localhost';
GRANT ALL PRIVILEGES ON *.* TO 'orderservice'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;

CREATE DATABASE restaurant_db;
CREATE USER 'restaurantservice'@'localhost' IDENTIFIED BY 'res';
GRANT ALL PRIVILEGES ON restaurant_db.* TO 'restaurantservice'@'localhost';
GRANT ALL PRIVILEGES ON *.* TO 'restaurantservice'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;

CREATE DATABASE payment_db;
CREATE USER 'paymentservice'@'localhost' IDENTIFIED BY 'pay';
GRANT ALL PRIVILEGES ON payment_db.* TO 'paymentservice'@'localhost';
GRANT ALL PRIVILEGES ON *.* TO 'paymentservice'@'localhost' WITH GRANT OPTION;
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
    is_active BOOLEAN NOT NULL DEFAULT TRUE
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

INSERT INTO users (address, email, is_active, password, profile_image_name, username) VALUES
('123 Street A', 'user1@example.com', 1, 'password1', NULL, 'user1'),
('123 Street B', 'user2@example.com', 1, 'password2', NULL, 'user2'),
('123 Street C', 'user3@example.com', 1, 'password3', NULL, 'user3'),
('123 Street D', 'user4@example.com', 1, 'password4', NULL, 'user4'),
('123 Street E', 'user5@example.com', 1, 'password5', NULL, 'user5'),
('123 Street F', 'user6@example.com', 1, 'password6', NULL, 'user6'),
('123 Street G', 'user7@example.com', 1, 'password7', NULL, 'user7'),
('123 Street H', 'user8@example.com', 1, 'password8', NULL, 'user8'),
('123 Street I', 'user9@example.com', 1, 'password9', NULL, 'user9'),
('123 Street J', 'user10@example.com', 1, 'password10', NULL, 'user10');

INSERT INTO user_roles (user_id, role)
SELECT user_id, 'ROLE_USER'
FROM users
WHERE username LIKE 'user%';