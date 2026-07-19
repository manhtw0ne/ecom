CREATE TABLE IF NOT EXISTS favorites (
                                         id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         user_id    BIGINT,
                                         product_id BIGINT,
                                         FOREIGN KEY (user_id)    REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
    );
