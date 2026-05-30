CREATE TABLE IF NOT EXISTS disclosure_group (
    dis_acct_group_id  VARCHAR(10)  NOT NULL,
    dis_tran_type_cd   VARCHAR(2)   NOT NULL,
    dis_tran_cat_cd    INT          NOT NULL,
    dis_int_rate       NUMERIC(6,2),
    PRIMARY KEY (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd)
);
