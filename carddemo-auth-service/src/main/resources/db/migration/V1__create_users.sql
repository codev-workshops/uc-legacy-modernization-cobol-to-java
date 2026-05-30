CREATE TABLE users (
    user_id         VARCHAR(8)   NOT NULL PRIMARY KEY,
    first_name      VARCHAR(20),
    last_name       VARCHAR(20),
    password        VARCHAR(255),
    user_type       VARCHAR(1),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
