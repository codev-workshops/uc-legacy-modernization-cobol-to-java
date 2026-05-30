package com.carddemo.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "tran_id", length = 16)
    private String tranId;

    @Column(name = "tran_type_cd", length = 2)
    private String tranTypeCd;

    @Column(name = "tran_cat_cd")
    private Integer tranCatCd;

    @Column(name = "tran_source", length = 10)
    private String tranSource;

    @Column(name = "tran_desc", length = 100)
    private String tranDesc;

    @Column(name = "tran_amt", precision = 11, scale = 2)
    private BigDecimal tranAmt;

    @Column(name = "tran_merchant_id")
    private Long tranMerchantId;

    @Column(name = "tran_merchant_name", length = 50)
    private String tranMerchantName;

    @Column(name = "tran_merchant_city", length = 50)
    private String tranMerchantCity;

    @Column(name = "tran_merchant_zip", length = 10)
    private String tranMerchantZip;

    @Column(name = "tran_card_num", length = 16)
    private String tranCardNum;

    @Column(name = "tran_orig_ts", length = 26)
    private String tranOrigTs;

    @Column(name = "tran_proc_ts", length = 26)
    private String tranProcTs;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
