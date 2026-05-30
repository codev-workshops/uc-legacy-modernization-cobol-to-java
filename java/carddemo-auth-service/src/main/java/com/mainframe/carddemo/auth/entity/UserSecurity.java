package com.mainframe.carddemo.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "user_security")
public class UserSecurity {

    @Id
    @Column(name = "usr_id", length = 8, nullable = false)
    private String usrId;

    @Column(name = "usr_fname", length = 20)
    private String usrFname;

    @Column(name = "usr_lname", length = 20)
    private String usrLname;

    @Column(name = "usr_pwd", length = 72)
    private String usrPwd;

    @Column(name = "usr_type", length = 1)
    private String usrType;

    @Column(name = "usr_filler", length = 23)
    private String usrFiller;

    public UserSecurity() {
    }

    public UserSecurity(String usrId, String usrFname, String usrLname, String usrPwd, String usrType) {
        this.usrId = usrId;
        this.usrFname = usrFname;
        this.usrLname = usrLname;
        this.usrPwd = usrPwd;
        this.usrType = usrType;
    }

    public String getUsrId() { return usrId; }
    public void setUsrId(String usrId) { this.usrId = usrId; }

    public String getUsrFname() { return usrFname; }
    public void setUsrFname(String usrFname) { this.usrFname = usrFname; }

    public String getUsrLname() { return usrLname; }
    public void setUsrLname(String usrLname) { this.usrLname = usrLname; }

    public String getUsrPwd() { return usrPwd; }
    public void setUsrPwd(String usrPwd) { this.usrPwd = usrPwd; }

    public String getUsrType() { return usrType; }
    public void setUsrType(String usrType) { this.usrType = usrType; }

    public String getUsrFiller() { return usrFiller; }
    public void setUsrFiller(String usrFiller) { this.usrFiller = usrFiller; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSecurity that = (UserSecurity) o;
        return Objects.equals(usrId, that.usrId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usrId);
    }
}
