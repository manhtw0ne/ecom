CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(350) NOT NULL,
    price FLOAT NOT NULL,
    thumbnail VARCHAR(300),
    description LONGTEXT,
    created_at DATETIME,
    updated_at DATETIME,
    category_id BIGINT,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT,
    image_url VARCHAR(300),
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

INSERT INTO categories (name) VALUES ('Electronics'), ('Clothing'), ('Home');