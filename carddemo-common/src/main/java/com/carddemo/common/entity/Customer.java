package com.carddemo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @Column(name = "cust_id")
    @NotNull
    private Long custId;

    @Column(name = "first_name", length = 25)
    @Size(max = 25)
    private String firstName;

    @Column(name = "middle_name", length = 25)
    @Size(max = 25)
    private String middleName;

    @Column(name = "last_name", length = 25)
    @Size(max = 25)
    private String lastName;

    @Column(name = "addr_line_1", length = 50)
    @Size(max = 50)
    private String addrLine1;

    @Column(name = "addr_line_2", length = 50)
    @Size(max = 50)
    private String addrLine2;

    @Column(name = "addr_line_3", length = 50)
    @Size(max = 50)
    private String addrLine3;

    @Column(name = "state_code", length = 2)
    @Size(max = 2)
    private String stateCode;

    @Column(name = "country_code", length = 3)
    @Size(max = 3)
    private String countryCode;

    @Column(name = "zip", length = 10)
    @Size(max = 10)
    private String zip;

    @Column(name = "phone1", length = 15)
    @Size(max = 15)
    private String phone1;

    @Column(name = "phone2", length = 15)
    @Size(max = 15)
    private String phone2;

    @Column(name = "ssn")
    private Long ssn;

    @Column(name = "govt_issued_id", length = 20)
    @Size(max = 20)
    private String govtIssuedId;

    @Column(name = "dob", length = 10)
    @Size(max = 10)
    private String dob;

    @Column(name = "eft_account_id", length = 10)
    @Size(max = 10)
    private String eftAccountId;

    @Column(name = "pri_card_holder_ind", length = 1)
    @Size(max = 1)
    private String priCardHolderInd;

    @Column(name = "fico_credit_score")
    private Integer ficoCreditScore;

    public Customer() {}

    public Long getCustId() { return custId; }
    public void setCustId(Long custId) { this.custId = custId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getAddrLine1() { return addrLine1; }
    public void setAddrLine1(String addrLine1) { this.addrLine1 = addrLine1; }
    public String getAddrLine2() { return addrLine2; }
    public void setAddrLine2(String addrLine2) { this.addrLine2 = addrLine2; }
    public String getAddrLine3() { return addrLine3; }
    public void setAddrLine3(String addrLine3) { this.addrLine3 = addrLine3; }
    public String getStateCode() { return stateCode; }
    public void setStateCode(String stateCode) { this.stateCode = stateCode; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }
    public String getPhone1() { return phone1; }
    public void setPhone1(String phone1) { this.phone1 = phone1; }
    public String getPhone2() { return phone2; }
    public void setPhone2(String phone2) { this.phone2 = phone2; }
    public Long getSsn() { return ssn; }
    public void setSsn(Long ssn) { this.ssn = ssn; }
    public String getGovtIssuedId() { return govtIssuedId; }
    public void setGovtIssuedId(String govtIssuedId) { this.govtIssuedId = govtIssuedId; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
    public String getEftAccountId() { return eftAccountId; }
    public void setEftAccountId(String eftAccountId) { this.eftAccountId = eftAccountId; }
    public String getPriCardHolderInd() { return priCardHolderInd; }
    public void setPriCardHolderInd(String priCardHolderInd) { this.priCardHolderInd = priCardHolderInd; }
    public Integer getFicoCreditScore() { return ficoCreditScore; }
    public void setFicoCreditScore(Integer ficoCreditScore) { this.ficoCreditScore = ficoCreditScore; }
}
