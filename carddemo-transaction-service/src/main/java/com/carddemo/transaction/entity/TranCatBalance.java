package com.carddemo.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@Table(name = "tran_cat_balance")
@IdClass(TranCatBalanceId.class)
public class TranCatBalance {

    @Id
    @Column(name = "trancat_acct_id")
    private Long trancatAcctId;

    @Id
    @Column(name = "trancat_type_cd", length = 2)
    private String trancatTypeCd;

    @Id
    @Column(name = "trancat_cd")
    private Integer trancatCd;

    @Column(name = "tran_cat_bal", precision = 11, scale = 2)
    private BigDecimal tranCatBal;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
