CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    fullname VARCHAR(100),
    email VARCHAR(100),
    phone_number VARCHAR(100) NOT NULL,
    address VARCHAR(100),
    note VARCHAR(100),
    order_date DATETIME,
    status VARCHAR(50),
    total_money FLOAT,
    shipping_method VARCHAR(100),
    shipping_address VARCHAR(100),
    shipping_date DATE,
    tracking_number VARCHAR(100),
    payment_method VARCHAR(100),
    active TINYINT(1) DEFAULT 1,
    vnp_txn_ref VARCHAR(50),
    coupon_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS order_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    product_id BIGINT,
    price FLOAT NOT NULL,
    number_of_products INT NOT NULL,
    total_money FLOAT NOT NULL,
    color VARCHAR(50),
    coupon_id BIGINT,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
)