package com.carddemo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Migrated from CVTRA06Y.cpy — DALYTRAN-RECORD (350-byte record).
 * Same structure as TransactionRecord but used for daily batch processing.
 */
public class DailyTransactionRecord {

    private String dalytranId;              // PIC X(16)
    private String dalytranTypeCd;          // PIC X(02)
    private int dalytranCatCd;              // PIC 9(04)
    private String dalytranSource;          // PIC X(10)
    private String dalytranDesc;            // PIC X(100)
    private BigDecimal dalytranAmt;         // PIC S9(09)V99
    private int dalytranMerchantId;         // PIC 9(09)
    private String dalytranMerchantName;    // PIC X(50)
    private String dalytranMerchantCity;    // PIC X(50)
    private String dalytranMerchantZip;     // PIC X(10)
    private String dalytranCardNum;         // PIC X(16)
    private LocalDateTime dalytranOrigTs;   // PIC X(26) timestamp
    private LocalDateTime dalytranProcTs;   // PIC X(26) timestamp

    public DailyTransactionRecord() {
    }

    public String getDalytranId() {
        return dalytranId;
    }

    public void setDalytranId(String dalytranId) {
        this.dalytranId = dalytranId;
    }

    public String getDalytranTypeCd() {
        return dalytranTypeCd;
    }

    public void setDalytranTypeCd(String dalytranTypeCd) {
        this.dalytranTypeCd = dalytranTypeCd;
    }

    public int getDalytranCatCd() {
        return dalytranCatCd;
    }

    public void setDalytranCatCd(int dalytranCatCd) {
        this.dalytranCatCd = dalytranCatCd;
    }

    public String getDalytranSource() {
        return dalytranSource;
    }

    public void setDalytranSource(String dalytranSource) {
        this.dalytranSource = dalytranSource;
    }

    public String getDalytranDesc() {
        return dalytranDesc;
    }

    public void setDalytranDesc(String dalytranDesc) {
        this.dalytranDesc = dalytranDesc;
    }

    public BigDecimal getDalytranAmt() {
        return dalytranAmt;
    }

    public void setDalytranAmt(BigDecimal dalytranAmt) {
        this.dalytranAmt = dalytranAmt;
    }

    public int getDalytranMerchantId() {
        return dalytranMerchantId;
    }

    public void setDalytranMerchantId(int dalytranMerchantId) {
        this.dalytranMerchantId = dalytranMerchantId;
    }

    public String getDalytranMerchantName() {
        return dalytranMerchantName;
    }

    public void setDalytranMerchantName(String dalytranMerchantName) {
        this.dalytranMerchantName = dalytranMerchantName;
    }

    public String getDalytranMerchantCity() {
        return dalytranMerchantCity;
    }

    public void setDalytranMerchantCity(String dalytranMerchantCity) {
        this.dalytranMerchantCity = dalytranMerchantCity;
    }

    public String getDalytranMerchantZip() {
        return dalytranMerchantZip;
    }

    public void setDalytranMerchantZip(String dalytranMerchantZip) {
        this.dalytranMerchantZip = dalytranMerchantZip;
    }

    public String getDalytranCardNum() {
        return dalytranCardNum;
    }

    public void setDalytranCardNum(String dalytranCardNum) {
        this.dalytranCardNum = dalytranCardNum;
    }

    public LocalDateTime getDalytranOrigTs() {
        return dalytranOrigTs;
    }

    public void setDalytranOrigTs(LocalDateTime dalytranOrigTs) {
        this.dalytranOrigTs = dalytranOrigTs;
    }

    public LocalDateTime getDalytranProcTs() {
        return dalytranProcTs;
    }

    public void setDalytranProcTs(LocalDateTime dalytranProcTs) {
        this.dalytranProcTs = dalytranProcTs;
    }
}
