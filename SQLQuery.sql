CREATE DATABASE ShopDB;
USE ShopDB

INSERT INTO category (category_name, created_at) VALUES
('Laptop', GETDATE()),
('Chuột', GETDATE()),
('Bàn phím', GETDATE()),
('Tai nghe', GETDATE());

INSERT INTO store (id, address, commune, district, province, description, total_stock) VALUES
(1, '123 Nguyen Trai', 'Thanh Xuan', 'Ha Noi', 'Ha Noi', 'Tech Store Ha Noi', 0);

INSERT INTO accounts (id, email, name, password, provider, provider_id, role) VALUES
(1, 'admin@gmail.com', 'Admin', '$2a$10$hashed', 'local', NULL, 'ADMIN'),
(2, 'user1@gmail.com', 'User One', '$2a$10$hashed', 'local', NULL, 'USER');

INSERT INTO users (id, birthday) VALUES
(1, '2000-01-01'),
(2, '2002-05-10');
INSERT INTO products (id, name, description, base_price, discount_percent, image_url, created_at, updated_at, category_id) VALUES
(1, 'Laptop Dell XPS 13', 'Laptop cao cap', 25000000, 10, 'laptop.jpg', NOW(), NOW(), 1),
(2, 'Chuột Logitech G102', 'Chuột gaming', 500000, 0, 'mouse.jpg', NOW(), NOW(), 2),
(3, 'Bàn phím Keychron K6', 'Bàn phím cơ', 2500000, 5, 'keyboard.jpg', NOW(), NOW(), 3),
(4, 'Tai nghe Sony WH-1000XM4', 'Tai nghe chống ồn', 7000000, 15, 'headphone.jpg', NOW(), NOW(), 4);

INSERT INTO product_variant (id, price, sku, stock, product_id) VALUES
(1, 23000000, 'LAPTOP-8GB', 5, 1),
(2, 26000000, 'LAPTOP-16GB', 3, 1),
(3, 500000, 'MOUSE-BLACK', 20, 2),
(4, 2500000, 'KEYBOARD-RGB', 10, 3),
(5, 7000000, 'HEADPHONE-BLACK', 8, 4);

INSERT INTO product_variant_option (id, attribute, value, product_variant_id) VALUES
(1, 'RAM', '8GB', 1),
(2, 'RAM', '16GB', 2),
(3, 'Color', 'Black', 3),
(4, 'Switch', 'RGB', 4),
(5, 'Color', 'Black', 5);

INSERT INTO store_product_variant (stock, product_variant_id, store_id) VALUES
(5, 1, 1),
(3, 2, 1),
(20, 3, 1),
(10, 4, 1),
(8, 5, 1);

INSERT INTO voucher (id, code, description, discount_percent, is_active, created_at, updated_at) VALUES
(1, 'SALE10', 'Giảm 10%', 10, true, NOW(), NOW()),
(2, 'SALE20', 'Giảm 20%', 20, true, NOW(), NOW());

INSERT INTO orders (id, address, commune, district, province, created_at, email, note, order_status, payment_method, payment_status, phone, receiver_name, shipping_fee, total_amount, updated_at, user_id) VALUES
(1, '123 Nguyen Trai', 'Thanh Xuan', 'Ha Noi', 'Ha Noi', NOW(), 'user1@gmail.com', 'Giao nhanh', 'PENDING', 'VNPAY', 'PENDING', '0123456789', 'User One', 30000, 23030000, NOW(), 2);

INSERT INTO order_item (id, quantity, total_price, unit_price_after_discount, order_id, product_variant_id) VALUES
(1, 1, 23000000, 23000000, 1, 1);

INSERT INTO order_voucher (order_id, voucher_id, used_at) VALUES
(1, 1, NOW());

INSERT INTO payments (id, amount, created_at, order_id, payment_method, session_id, transaction_id) VALUES
(1, 23030000, NOW(), 1, 'VNPAY', 'SESSION123', 'TRANS123');