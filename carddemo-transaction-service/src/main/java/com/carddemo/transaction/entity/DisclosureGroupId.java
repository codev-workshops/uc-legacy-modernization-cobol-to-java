package com.carddemo.transaction.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisclosureGroupId implements Serializable {

    private String disAcctGroupId;
    private String disTranTypeCd;
    private Integer disTranCatCd;
}
