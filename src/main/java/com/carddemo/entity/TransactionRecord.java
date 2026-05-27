package com.carddemo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Migrated from CVTRA05Y.cpy — TRAN-RECORD (350-byte record).
 */
public class TransactionRecord {

    private String tranId;                  // PIC X(16)
    private String tranTypeCd;              // PIC X(02)
    private int tranCatCd;                  // PIC 9(04)
    private String tranSource;              // PIC X(10)
    private String tranDesc;                // PIC X(100)
    private BigDecimal tranAmt;             // PIC S9(09)V99
    private int tranMerchantId;             // PIC 9(09)
    private String tranMerchantName;        // PIC X(50)
    private String tranMerchantCity;        // PIC X(50)
    private String tranMerchantZip;         // PIC X(10)
    private String tranCardNum;             // PIC X(16)
    private LocalDateTime tranOrigTs;       // PIC X(26) timestamp
    private LocalDateTime tranProcTs;       // PIC X(26) timestamp

    public TransactionRecord() {
    }

    public String getTranId() {
        return tranId;
    }

    public void setTranId(String tranId) {
        this.tranId = tranId;
    }

    public String getTranTypeCd() {
        return tranTypeCd;
    }

    public void setTranTypeCd(String tranTypeCd) {
        this.tranTypeCd = tranTypeCd;
    }

    public int getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(int tranCatCd) {
        this.tranCatCd = tranCatCd;
    }

    public String getTranSource() {
        return tranSource;
    }

    public void setTranSource(String tranSource) {
        this.tranSource = tranSource;
    }

    public String getTranDesc() {
        return tranDesc;
    }

    public void setTranDesc(String tranDesc) {
        this.tranDesc = tranDesc;
    }

    public BigDecimal getTranAmt() {
        return tranAmt;
    }

    public void setTranAmt(BigDecimal tranAmt) {
        this.tranAmt = tranAmt;
    }

    public int getTranMerchantId() {
        return tranMerchantId;
    }

    public void setTranMerchantId(int tranMerchantId) {
        this.tranMerchantId = tranMerchantId;
    }

    public String getTranMerchantName() {
        return tranMerchantName;
    }

    public void setTranMerchantName(String tranMerchantName) {
        this.tranMerchantName = tranMerchantName;
    }

    public String getTranMerchantCity() {
        return tranMerchantCity;
    }

    public void setTranMerchantCity(String tranMerchantCity) {
        this.tranMerchantCity = tranMerchantCity;
    }

    public String getTranMerchantZip() {
        return tranMerchantZip;
    }

    public void setTranMerchantZip(String tranMerchantZip) {
        this.tranMerchantZip = tranMerchantZip;
    }

    public String getTranCardNum() {
        return tranCardNum;
    }

    public void setTranCardNum(String tranCardNum) {
        this.tranCardNum = tranCardNum;
    }

    public LocalDateTime getTranOrigTs() {
        return tranOrigTs;
    }

    public void setTranOrigTs(LocalDateTime tranOrigTs) {
        this.tranOrigTs = tranOrigTs;
    }

    public LocalDateTime getTranProcTs() {
        return tranProcTs;
    }

    public void setTranProcTs(LocalDateTime tranProcTs) {
        this.tranProcTs = tranProcTs;
    }
}
