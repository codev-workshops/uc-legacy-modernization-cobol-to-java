package com.carddemo.entity;

/**
 * Migrated from COCOM01Y.cpy — CARDDEMO-COMMAREA.
 * Communication area with nested groups for the CardDemo application.
 */
public class CardDemoCommarea {

    // CDEMO-GENERAL-INFO group
    private String cdemoFromTranid;         // PIC X(04)
    private String cdemoFromProgram;        // PIC X(08)
    private String cdemoToTranid;           // PIC X(04)
    private String cdemoToProgram;          // PIC X(08)
    private String cdemoUserId;             // PIC X(08)
    private String cdemoUserType;           // PIC X(01) — 'A' = admin, 'U' = user
    private int cdemoPgmContext;            // PIC 9(01) — 0 = enter, 1 = reenter

    // CDEMO-CUSTOMER-INFO group
    private int cdemoCustId;                // PIC 9(09)
    private String cdemoCustFname;          // PIC X(25)
    private String cdemoCustMname;          // PIC X(25)
    private String cdemoCustLname;          // PIC X(25)

    // CDEMO-ACCOUNT-INFO group
    private long cdemoAcctId;               // PIC 9(11)
    private String cdemoAcctStatus;         // PIC X(01)

    // CDEMO-CARD-INFO group
    private long cdemoCardNum;              // PIC 9(16)

    // CDEMO-MORE-INFO group
    private String cdemoLastMap;            // PIC X(7)
    private String cdemoLastMapset;         // PIC X(7)

    public CardDemoCommarea() {
    }

    public String getCdemoFromTranid() {
        return cdemoFromTranid;
    }

    public void setCdemoFromTranid(String cdemoFromTranid) {
        this.cdemoFromTranid = cdemoFromTranid;
    }

    public String getCdemoFromProgram() {
        return cdemoFromProgram;
    }

    public void setCdemoFromProgram(String cdemoFromProgram) {
        this.cdemoFromProgram = cdemoFromProgram;
    }

    public String getCdemoToTranid() {
        return cdemoToTranid;
    }

    public void setCdemoToTranid(String cdemoToTranid) {
        this.cdemoToTranid = cdemoToTranid;
    }

    public String getCdemoToProgram() {
        return cdemoToProgram;
    }

    public void setCdemoToProgram(String cdemoToProgram) {
        this.cdemoToProgram = cdemoToProgram;
    }

    public String getCdemoUserId() {
        return cdemoUserId;
    }

    public void setCdemoUserId(String cdemoUserId) {
        this.cdemoUserId = cdemoUserId;
    }

    public String getCdemoUserType() {
        return cdemoUserType;
    }

    public void setCdemoUserType(String cdemoUserType) {
        this.cdemoUserType = cdemoUserType;
    }

    public int getCdemoPgmContext() {
        return cdemoPgmContext;
    }

    public void setCdemoPgmContext(int cdemoPgmContext) {
        this.cdemoPgmContext = cdemoPgmContext;
    }

    public int getCdemoCustId() {
        return cdemoCustId;
    }

    public void setCdemoCustId(int cdemoCustId) {
        this.cdemoCustId = cdemoCustId;
    }

    public String getCdemoCustFname() {
        return cdemoCustFname;
    }

    public void setCdemoCustFname(String cdemoCustFname) {
        this.cdemoCustFname = cdemoCustFname;
    }

    public String getCdemoCustMname() {
        return cdemoCustMname;
    }

    public void setCdemoCustMname(String cdemoCustMname) {
        this.cdemoCustMname = cdemoCustMname;
    }

    public String getCdemoCustLname() {
        return cdemoCustLname;
    }

    public void setCdemoCustLname(String cdemoCustLname) {
        this.cdemoCustLname = cdemoCustLname;
    }

    public long getCdemoAcctId() {
        return cdemoAcctId;
    }

    public void setCdemoAcctId(long cdemoAcctId) {
        this.cdemoAcctId = cdemoAcctId;
    }

    public String getCdemoAcctStatus() {
        return cdemoAcctStatus;
    }

    public void setCdemoAcctStatus(String cdemoAcctStatus) {
        this.cdemoAcctStatus = cdemoAcctStatus;
    }

    public long getCdemoCardNum() {
        return cdemoCardNum;
    }

    public void setCdemoCardNum(long cdemoCardNum) {
        this.cdemoCardNum = cdemoCardNum;
    }

    public String getCdemoLastMap() {
        return cdemoLastMap;
    }

    public void setCdemoLastMap(String cdemoLastMap) {
        this.cdemoLastMap = cdemoLastMap;
    }

    public String getCdemoLastMapset() {
        return cdemoLastMapset;
    }

    public void setCdemoLastMapset(String cdemoLastMapset) {
        this.cdemoLastMapset = cdemoLastMapset;
    }
}
