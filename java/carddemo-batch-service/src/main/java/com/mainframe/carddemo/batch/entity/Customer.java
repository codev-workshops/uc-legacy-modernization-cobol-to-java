package com.mainframe.carddemo.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @Column(name = "cust_id", nullable = false)
    private Long custId;

    @Column(name = "cust_first_name", length = 25)
    private String custFirstName;

    @Column(name = "cust_middle_name", length = 25)
    private String custMiddleName;

    @Column(name = "cust_last_name", length = 25)
    private String custLastName;

    @Column(name = "cust_addr_line_1", length = 50)
    private String custAddrLine1;

    @Column(name = "cust_addr_line_2", length = 50)
    private String custAddrLine2;

    @Column(name = "cust_addr_line_3", length = 50)
    private String custAddrLine3;

    @Column(name = "cust_addr_state_cd", length = 2)
    private String custAddrStateCd;

    @Column(name = "cust_addr_country_cd", length = 3)
    private String custAddrCountryCd;

    @Column(name = "cust_addr_zip", length = 10)
    private String custAddrZip;

    @Column(name = "cust_phone_num_1", length = 15)
    private String custPhoneNum1;

    @Column(name = "cust_phone_num_2", length = 15)
    private String custPhoneNum2;

    @Column(name = "cust_ssn")
    private Long custSsn;

    @Column(name = "cust_govt_issued_id", length = 20)
    private String custGovtIssuedId;

    @Column(name = "cust_dob")
    private LocalDate custDob;

    @Column(name = "cust_eft_account_id", length = 10)
    private String custEftAccountId;

    @Column(name = "cust_pri_card_holder_ind", length = 1)
    private String custPriCardHolderInd;

    @Column(name = "cust_fico_credit_score")
    private Integer custFicoCreditScore;

    public Customer() {}

    public Long getCustId() { return custId; }
    public void setCustId(Long v) { this.custId = v; }

    public String getCustFirstName() { return custFirstName; }
    public void setCustFirstName(String v) { this.custFirstName = v; }

    public String getCustMiddleName() { return custMiddleName; }
    public void setCustMiddleName(String v) { this.custMiddleName = v; }

    public String getCustLastName() { return custLastName; }
    public void setCustLastName(String v) { this.custLastName = v; }

    public String getCustAddrLine1() { return custAddrLine1; }
    public void setCustAddrLine1(String v) { this.custAddrLine1 = v; }

    public String getCustAddrLine2() { return custAddrLine2; }
    public void setCustAddrLine2(String v) { this.custAddrLine2 = v; }

    public String getCustAddrLine3() { return custAddrLine3; }
    public void setCustAddrLine3(String v) { this.custAddrLine3 = v; }

    public String getCustAddrStateCd() { return custAddrStateCd; }
    public void setCustAddrStateCd(String v) { this.custAddrStateCd = v; }

    public String getCustAddrCountryCd() { return custAddrCountryCd; }
    public void setCustAddrCountryCd(String v) { this.custAddrCountryCd = v; }

    public String getCustAddrZip() { return custAddrZip; }
    public void setCustAddrZip(String v) { this.custAddrZip = v; }

    public String getCustPhoneNum1() { return custPhoneNum1; }
    public void setCustPhoneNum1(String v) { this.custPhoneNum1 = v; }

    public String getCustPhoneNum2() { return custPhoneNum2; }
    public void setCustPhoneNum2(String v) { this.custPhoneNum2 = v; }

    public Long getCustSsn() { return custSsn; }
    public void setCustSsn(Long v) { this.custSsn = v; }

    public String getCustGovtIssuedId() { return custGovtIssuedId; }
    public void setCustGovtIssuedId(String v) { this.custGovtIssuedId = v; }

    public LocalDate getCustDob() { return custDob; }
    public void setCustDob(LocalDate v) { this.custDob = v; }

    public String getCustEftAccountId() { return custEftAccountId; }
    public void setCustEftAccountId(String v) { this.custEftAccountId = v; }

    public String getCustPriCardHolderInd() { return custPriCardHolderInd; }
    public void setCustPriCardHolderInd(String v) { this.custPriCardHolderInd = v; }

    public Integer getCustFicoCreditScore() { return custFicoCreditScore; }
    public void setCustFicoCreditScore(Integer v) { this.custFicoCreditScore = v; }
}
