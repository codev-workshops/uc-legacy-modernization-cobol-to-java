CREATE TABLE IF NOT EXISTS tran_category (
    tran_type_cd        VARCHAR(2)   NOT NULL,
    tran_cat_cd         INT          NOT NULL,
    tran_cat_type_desc  VARCHAR(50),
    PRIMARY KEY (tran_type_cd, tran_cat_cd)
);
