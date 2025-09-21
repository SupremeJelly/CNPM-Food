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
