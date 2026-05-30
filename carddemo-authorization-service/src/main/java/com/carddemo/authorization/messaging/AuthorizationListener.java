package com.carddemo.authorization.messaging;

import com.carddemo.authorization.config.RabbitMQConfig;
import com.carddemo.authorization.entity.Authorization;
import com.carddemo.authorization.repository.AuthorizationRepository;
import com.carddemo.common.client.AccountServiceClient;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.dto.CardXrefDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class AuthorizationListener {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationListener.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final AuthorizationRepository authorizationRepository;
    private final AccountServiceClient accountServiceClient;
    private final RabbitTemplate rabbitTemplate;

    public AuthorizationListener(AuthorizationRepository authorizationRepository,
                                 AccountServiceClient accountServiceClient,
                                 RabbitTemplate rabbitTemplate) {
        this.authorizationRepository = authorizationRepository;
        this.accountServiceClient = accountServiceClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.AUTH_REQUEST_QUEUE)
    @CircuitBreaker(name = "accountService", fallbackMethod = "processAuthFallback")
    public void processAuthorizationRequest(String message) {
        log.info("Received authorization request: {}", message);
        try {
            String[] fields = parseCsvFields(message);
            if (fields.length < 18) {
                log.error("Malformed authorization request: expected 18 fields, got {}", fields.length);
                publishDeclinedResponse(fields.length > 2 ? fields[2] : "UNKNOWN",
                        fields.length > 17 ? fields[17] : "UNKNOWN",
                        "MALF");
                return;
            }

            String authDate = fields[0].trim();
            String authTime = fields[1].trim();
            String cardNum = fields[2].trim();
            String authType = fields[3].trim();
            String cardExpiryDate = fields[4].trim();
            String messageType = fields[5].trim();
            String messageSource = fields[6].trim();
            String processingCode = fields[7].trim();
            BigDecimal transactionAmt = new BigDecimal(fields[8].trim());
            String merchantCategoryCode = fields[9].trim();
            String acqrCountryCode = fields[10].trim();
            Short posEntryMode = Short.parseShort(fields[11].trim());
            String merchantId = fields[12].trim();
            String merchantName = fields[13].trim();
            String merchantCity = fields[14].trim();
            String merchantState = fields[15].trim();
            String merchantZip = fields[16].trim();
            String transactionId = fields[17].trim();

            LocalDateTime authTs = parseAuthTimestamp(authDate, authTime);

            CardXrefDto xref = accountServiceClient.getCardXref(cardNum);
            AccountDto account = accountServiceClient.getAccount(xref.getXrefAcctId());

            String authRespCode;
            String authRespReason;
            BigDecimal approvedAmt;

            if (isCardExpired(cardExpiryDate)) {
                authRespCode = "05";
                authRespReason = "EXPD";
                approvedAmt = BigDecimal.ZERO;
                log.info("Authorization declined for card {}: card expired", cardNum);
            } else if (!"Y".equals(account.getAcctActiveStatus())) {
                authRespCode = "05";
                authRespReason = "INAC";
                approvedAmt = BigDecimal.ZERO;
                log.info("Authorization declined for card {}: account inactive", cardNum);
            } else if (account.getAcctCreditLimit() != null
                    && account.getAcctCurrBal() != null
                    && account.getAcctCurrBal().add(transactionAmt).compareTo(account.getAcctCreditLimit()) > 0) {
                authRespCode = "05";
                authRespReason = "OVLM";
                approvedAmt = BigDecimal.ZERO;
                log.info("Authorization declined for card {}: over credit limit", cardNum);
            } else {
                authRespCode = "00";
                authRespReason = "APRV";
                approvedAmt = transactionAmt;
                log.info("Authorization approved for card {}: amount {}", cardNum, transactionAmt);
            }

            String authIdCode = generateAuthIdCode();

            Authorization authorization = Authorization.builder()
                    .cardNum(cardNum)
                    .authTs(authTs)
                    .authType(authType)
                    .cardExpiryDate(cardExpiryDate)
                    .messageType(messageType)
                    .messageSource(messageSource)
                    .authIdCode(authIdCode)
                    .authRespCode(authRespCode)
                    .authRespReason(authRespReason)
                    .processingCode(processingCode)
                    .transactionAmt(transactionAmt)
                    .approvedAmt(approvedAmt)
                    .merchantCategoryCode(merchantCategoryCode)
                    .acqrCountryCode(acqrCountryCode)
                    .posEntryMode(posEntryMode)
                    .merchantId(merchantId)
                    .merchantName(merchantName)
                    .merchantCity(merchantCity)
                    .merchantState(merchantState)
                    .merchantZip(merchantZip)
                    .transactionId(transactionId)
                    .matchStatus("N")
                    .acctId(xref.getXrefAcctId())
                    .custId(xref.getXrefCustId())
                    .createdAt(LocalDateTime.now())
                    .build();

            authorizationRepository.save(authorization);
            log.info("Authorization saved with ID: {}", authorization.getAuthId());

            String response = String.join(",",
                    cardNum, transactionId, authIdCode, authRespCode, authRespReason, approvedAmt.toPlainString());
            rabbitTemplate.convertAndSend(RabbitMQConfig.AUTH_REPLY_QUEUE, response);
            log.info("Authorization response published: {}", response);

        } catch (Exception e) {
            log.error("Error processing authorization request: {}", e.getMessage(), e);
            throw e;
        }
    }

    String[] parseCsvFields(String message) {
        return message.split(",", -1);
    }

    LocalDateTime parseAuthTimestamp(String date, String time) {
        try {
            return LocalDateTime.parse(date + "T" + time);
        } catch (Exception e) {
            log.warn("Could not parse timestamp from date='{}' time='{}', using current time", date, time);
            return LocalDateTime.now();
        }
    }

    boolean isCardExpired(String expiryDate) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            return false;
        }
        try {
            String trimmed = expiryDate.trim();
            int month;
            int year;
            if (trimmed.length() == 4) {
                month = Integer.parseInt(trimmed.substring(0, 2));
                year = 2000 + Integer.parseInt(trimmed.substring(2, 4));
            } else {
                return false;
            }
            LocalDateTime expiry = LocalDateTime.of(year, month, 1, 0, 0).plusMonths(1);
            return LocalDateTime.now().isAfter(expiry);
        } catch (NumberFormatException e) {
            log.warn("Invalid expiry date format: {}", expiryDate);
            return false;
        }
    }

    String generateAuthIdCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private void publishDeclinedResponse(String cardNum, String transactionId, String reason) {
        String response = String.join(",",
                cardNum, transactionId, "000000", "05", reason, "0");
        rabbitTemplate.convertAndSend(RabbitMQConfig.AUTH_REPLY_QUEUE, response);
    }

    @SuppressWarnings("unused")
    public void processAuthFallback(String message, Throwable t) {
        log.error("Circuit breaker fallback for authorization processing: {}", t.getMessage());
        String[] fields = parseCsvFields(message);
        publishDeclinedResponse(
                fields.length > 2 ? fields[2].trim() : "UNKNOWN",
                fields.length > 17 ? fields[17].trim() : "UNKNOWN",
                "SYSE");
    }
}
