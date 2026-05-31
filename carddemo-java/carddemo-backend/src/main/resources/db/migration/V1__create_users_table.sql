CREATE TABLE users (
    user_id VARCHAR(8) PRIMARY KEY,
    first_name VARCHAR(20) NOT NULL,
    last_name VARCHAR(20) NOT NULL,
    password_hash VARCHAR(72) NOT NULL,
    user_type VARCHAR(1) NOT NULL CHECK (user_type IN ('A','U')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
