package com.carddemo.online.dto;

public class UserResponse {

    private String usrId;
    private String fname;
    private String lname;
    private String usrType;

    public UserResponse() {}

    public UserResponse(String usrId, String fname, String lname, String usrType) {
        this.usrId = usrId;
        this.fname = fname;
        this.lname = lname;
        this.usrType = usrType;
    }

    public String getUsrId() { return usrId; }
    public void setUsrId(String usrId) { this.usrId = usrId; }
    public String getFname() { return fname; }
    public void setFname(String fname) { this.fname = fname; }
    public String getLname() { return lname; }
    public void setLname(String lname) { this.lname = lname; }
    public String getUsrType() { return usrType; }
    public void setUsrType(String usrType) { this.usrType = usrType; }
}
