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
@Table(name = "disclosure_groups")
@IdClass(DisclosureGroupId.class)
public class DisclosureGroup {

    @Id
    @Column(name = "dis_acct_group_id", length = 10)
    private String disAcctGroupId;

    @Id
    @Column(name = "dis_tran_type_cd", length = 2)
    private String disTranTypeCd;

    @Id
    @Column(name = "dis_tran_cat_cd")
    private Integer disTranCatCd;

    @Column(name = "dis_int_rate", precision = 6, scale = 2)
    private BigDecimal disIntRate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
