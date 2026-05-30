package com.carddemo.authorization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "authorizations")
public class Authorization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_id")
    private Integer authId;

    @Column(name = "card_num", nullable = false, length = 16)
    private String cardNum;

    @Column(name = "auth_ts", nullable = false)
    private LocalDateTime authTs;

    @Column(name = "auth_type", columnDefinition = "CHAR(4)")
    private String authType;

    @Column(name = "card_expiry_date", columnDefinition = "CHAR(4)")
    private String cardExpiryDate;

    @Column(name = "message_type", columnDefinition = "CHAR(6)")
    private String messageType;

    @Column(name = "message_source", columnDefinition = "CHAR(6)")
    private String messageSource;

    @Column(name = "auth_id_code", columnDefinition = "CHAR(6)")
    private String authIdCode;

    @Column(name = "auth_resp_code", columnDefinition = "CHAR(2)")
    private String authRespCode;

    @Column(name = "auth_resp_reason", columnDefinition = "CHAR(4)")
    private String authRespReason;

    @Column(name = "processing_code", columnDefinition = "CHAR(6)")
    private String processingCode;

    @Column(name = "transaction_amt", precision = 12, scale = 2)
    private BigDecimal transactionAmt;

    @Column(name = "approved_amt", precision = 12, scale = 2)
    private BigDecimal approvedAmt;

    @Column(name = "merchant_category_code", columnDefinition = "CHAR(4)")
    private String merchantCategoryCode;

    @Column(name = "acqr_country_code", columnDefinition = "CHAR(3)")
    private String acqrCountryCode;

    @Column(name = "pos_entry_mode")
    private Short posEntryMode;

    @Column(name = "merchant_id", length = 15)
    private String merchantId;

    @Column(name = "merchant_name", length = 22)
    private String merchantName;

    @Column(name = "merchant_city", length = 13)
    private String merchantCity;

    @Column(name = "merchant_state", columnDefinition = "CHAR(2)")
    private String merchantState;

    @Column(name = "merchant_zip", length = 9)
    private String merchantZip;

    @Column(name = "transaction_id", length = 15)
    private String transactionId;

    @Column(name = "match_status", columnDefinition = "CHAR(1)")
    private String matchStatus;

    @Column(name = "acct_id")
    private Long acctId;

    @Column(name = "cust_id")
    private Long custId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
