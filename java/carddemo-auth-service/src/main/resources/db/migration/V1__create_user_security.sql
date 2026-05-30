CREATE TABLE IF NOT EXISTS user_security (
    usr_id        VARCHAR(8)   NOT NULL PRIMARY KEY,
    usr_fname     VARCHAR(20),
    usr_lname     VARCHAR(20),
    usr_pwd       VARCHAR(72),
    usr_type      CHAR(1),
    usr_filler    VARCHAR(23)
);
