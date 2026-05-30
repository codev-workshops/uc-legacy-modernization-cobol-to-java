package com.carddemo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "usr_id", length = 8)
    @NotBlank
    @Size(max = 8)
    private String usrId;

    @Column(name = "first_name", length = 20)
    @Size(max = 20)
    private String fname;

    @Column(name = "last_name", length = 20)
    @Size(max = 20)
    private String lname;

    @Column(name = "password", length = 8)
    @Size(max = 8)
    private String pwd;

    @Column(name = "usr_type", length = 1)
    @Size(max = 1)
    private String usrType;

    public User() {}

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
