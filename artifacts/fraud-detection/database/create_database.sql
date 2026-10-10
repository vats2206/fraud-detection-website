CREATE DATABASE IF NOT EXISTS fraud_detection
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'fraud_app'@'127.0.0.1' IDENTIFIED BY 'choose-a-password';

GRANT ALL PRIVILEGES ON fraud_detection.* TO 'fraud_app'@'127.0.0.1';

FLUSH PRIVILEGES;
