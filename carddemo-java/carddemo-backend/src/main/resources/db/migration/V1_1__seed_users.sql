-- Seed admin user (password: admin123)
INSERT INTO users (user_id, first_name, last_name, password_hash, user_type)
VALUES ('ADMIN001', 'System', 'Admin',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'A');

-- Seed regular user (password: user1234)
INSERT INTO users (user_id, first_name, last_name, password_hash, user_type)
VALUES ('USER0001', 'John', 'Doe',
        '$2a$10$Dow5ElR0Gmtgxsny3mnzF.9qFERixhE7Ul0.p1sBg8IJt.3WhpGMy', 'U');

-- Seed second regular user (password: user5678)
INSERT INTO users (user_id, first_name, last_name, password_hash, user_type)
VALUES ('USER0002', 'Jane', 'Smith',
        '$2a$10$7QcJ2bI0rG5LfVKdpY0Xu.5Y3K6fM8vN2qW4eR1tY9uI3oP5sL7Ga', 'U');
