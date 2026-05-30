package com.carddemo.transaction.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranCatBalanceId implements Serializable {

    private Long trancatAcctId;
    private String trancatTypeCd;
    private Integer trancatCd;
}
