CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fullname VARCHAR(100),
    phone_number VARCHAR(100),
    email VARCHAR(255),
    address VARCHAR(200),
    profile_image VARCHAR(255),
    password VARCHAR(100),
    is_active TINYINT(1) DEFAULT 1,
    date_of_birth DATE,
    facebook_account_id VARCHAR(255),
    google_account_id VARCHAR(255)
    role_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

INSERT INTO roles (name) VALUES ("ADMIN");
INSERT INTO roles (name) VALUES ("USER");