package com.carddemo.authorization.service;

import com.carddemo.authorization.entity.Authorization;
import com.carddemo.authorization.repository.AuthorizationRepository;
import com.carddemo.common.client.AccountServiceClient;
import com.carddemo.common.dto.AccountDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthPurgeService {

    private static final Logger log = LoggerFactory.getLogger(AuthPurgeService.class);

    private final AuthorizationRepository authorizationRepository;
    private final AccountServiceClient accountServiceClient;

    @Value("${carddemo.auth-purge.days-threshold:90}")
    private int daysThreshold;

    public AuthPurgeService(AuthorizationRepository authorizationRepository,
                            AccountServiceClient accountServiceClient) {
        this.authorizationRepository = authorizationRepository;
        this.accountServiceClient = accountServiceClient;
    }

    @Transactional
    public int purgeExpiredAuthorizations() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysThreshold);
        log.info("Purging authorizations older than {} (threshold: {} days)", cutoff, daysThreshold);

        List<Authorization> expired = authorizationRepository.findByAuthTsBefore(cutoff);
        if (expired.isEmpty()) {
            log.info("No expired authorizations found");
            return 0;
        }

        for (Authorization auth : expired) {
            if ("U".equals(auth.getMatchStatus()) && auth.getApprovedAmt() != null
                    && auth.getApprovedAmt().compareTo(BigDecimal.ZERO) > 0
                    && auth.getAcctId() != null) {
                adjustAccountCredit(auth);
            }
        }

        authorizationRepository.deleteAll(expired);
        log.info("Purged {} expired authorizations", expired.size());
        return expired.size();
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "adjustCreditFallback")
    private void adjustAccountCredit(Authorization auth) {
        try {
            AccountDto account = accountServiceClient.getAccount(auth.getAcctId());
            BigDecimal newBal = account.getAcctCurrBal().subtract(auth.getApprovedAmt());
            account.setAcctCurrBal(newBal);
            accountServiceClient.updateAccount(auth.getAcctId(), account);
            log.info("Adjusted credit for account {} by -{}", auth.getAcctId(), auth.getApprovedAmt());
        } catch (Exception e) {
            log.warn("Failed to adjust credit for account {}: {}", auth.getAcctId(), e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    private void adjustCreditFallback(Authorization auth, Throwable t) {
        log.warn("Circuit breaker open for account service, skipping credit adjustment for account {}: {}",
                auth.getAcctId(), t.getMessage());
    }

    public void setDaysThreshold(int daysThreshold) {
        this.daysThreshold = daysThreshold;
    }
}
