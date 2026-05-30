package com.carddemo.online.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CardUpdateRequest {

    private Long acctId;

    private Integer cvvCd;

    @Size(max = 50)
    private String embossedName;

    @Size(max = 10)
    private String expirationDate;

    @Size(max = 1)
    @Pattern(regexp = "[YN]", message = "activeStatus must be Y or N")
    private String activeStatus;

    public CardUpdateRequest() {}

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }
    public Integer getCvvCd() { return cvvCd; }
    public void setCvvCd(Integer cvvCd) { this.cvvCd = cvvCd; }
    public String getEmbossedName() { return embossedName; }
    public void setEmbossedName(String embossedName) { this.embossedName = embossedName; }
    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
}
