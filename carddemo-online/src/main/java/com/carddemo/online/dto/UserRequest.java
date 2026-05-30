package com.carddemo.online.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRequest {

    @NotBlank
    @Size(max = 8)
    private String usrId;

    @Size(max = 20)
    private String fname;

    @Size(max = 20)
    private String lname;

    @NotBlank
    @Size(max = 8)
    private String pwd;

    @NotBlank
    @Pattern(regexp = "[AU]")
    private String usrType;

    public UserRequest() {}

    public String getUsrId() { return usrId; }
    public void setUsrId(String usrId) { this.usrId = usrId; }
    public String getFname() { return fname; }
    public void setFname(String fname) { this.fname = fname; }
    public String getLname() { return lname; }
    public void setLname(String lname) { this.lname = lname; }
    public String getPwd() { return pwd; }
    public void setPwd(String pwd) { this.pwd = pwd; }
    public String getUsrType() { return usrType; }
    public void setUsrType(String usrType) { this.usrType = usrType; }
}
