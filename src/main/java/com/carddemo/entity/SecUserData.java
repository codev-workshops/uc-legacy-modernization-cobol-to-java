package com.carddemo.entity;

/**
 * Migrated from CSUSR01Y.cpy — SEC-USER-DATA.
 * Security user record.
 */
public class SecUserData {

    private String secUsrId;            // PIC X(08)
    private String secUsrFname;         // PIC X(20)
    private String secUsrLname;         // PIC X(20)
    // Password field — requires bcrypt hashing before persistence
    private String secUsrPwd;           // PIC X(08)
    private String secUsrType;          // PIC X(01)

    public SecUserData() {
    }

    public String getSecUsrId() {
        return secUsrId;
    }

    public void setSecUsrId(String secUsrId) {
        this.secUsrId = secUsrId;
    }

    public String getSecUsrFname() {
        return secUsrFname;
    }

    public void setSecUsrFname(String secUsrFname) {
        this.secUsrFname = secUsrFname;
    }

    public String getSecUsrLname() {
        return secUsrLname;
    }

    public void setSecUsrLname(String secUsrLname) {
        this.secUsrLname = secUsrLname;
    }

    public String getSecUsrPwd() {
        return secUsrPwd;
    }

    public void setSecUsrPwd(String secUsrPwd) {
        this.secUsrPwd = secUsrPwd;
    }

    public String getSecUsrType() {
        return secUsrType;
    }

    public void setSecUsrType(String secUsrType) {
        this.secUsrType = secUsrType;
    }
}
