CREATE TABLE IF NOT EXISTS tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255),
    refresh_token VARCHAR(255),
    token_type VARCHAR(50),
    expiration_date DATETIME,
    refresh_expiration_date DATETIME,
    is_mobile TINYINT(1) DEFAULT 0,
    revoked TINYINT(1) DEFAULT 0,
    expired TINYINT(1) DEFAULT 0,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);