package com.carddemo.authorization.service;

import com.carddemo.authorization.entity.Authorization;
import com.carddemo.authorization.repository.AuthorizationRepository;
import com.carddemo.common.client.AccountServiceClient;
import com.carddemo.common.dto.AccountDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthPurgeServiceTest {

    @Mock
    private AuthorizationRepository authorizationRepository;
    @Mock
    private AccountServiceClient accountServiceClient;

    private AuthPurgeService service;

    @BeforeEach
    void setUp() {
        service = new AuthPurgeService(authorizationRepository, accountServiceClient);
        service.setDaysThreshold(90);
    }

    @Test
    void purgeExpiredAuthorizations_noExpired() {
        when(authorizationRepository.findByAuthTsBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        int result = service.purgeExpiredAuthorizations();
        assertEquals(0, result);
        verify(authorizationRepository, never()).deleteAll(any());
    }

    @Test
    void purgeExpiredAuthorizations_withMatchedAuths() {
        Authorization auth = Authorization.builder()
                .authId(1)
                .cardNum("4111111111111111")
                .authTs(LocalDateTime.now().minusDays(100))
                .matchStatus("M")
                .approvedAmt(new BigDecimal("100.00"))
                .acctId(1001L)
                .build();

        when(authorizationRepository.findByAuthTsBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(auth));

        int result = service.purgeExpiredAuthorizations();
        assertEquals(1, result);
        verify(authorizationRepository).deleteAll(List.of(auth));
        verify(accountServiceClient, never()).getAccount(any());
    }

    @Test
    void purgeExpiredAuthorizations_withUnmatchedAuth_adjustsCredit() {
        Authorization auth = Authorization.builder()
                .authId(1)
                .cardNum("4111111111111111")
                .authTs(LocalDateTime.now().minusDays(100))
                .matchStatus("U")
                .approvedAmt(new BigDecimal("100.00"))
                .acctId(1001L)
                .build();

        AccountDto account = AccountDto.builder()
                .acctId(1001L)
                .acctCurrBal(new BigDecimal("500.00"))
                .build();

        when(authorizationRepository.findByAuthTsBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(auth));
        when(accountServiceClient.getAccount(1001L)).thenReturn(account);
        when(accountServiceClient.updateAccount(eq(1001L), any(AccountDto.class))).thenReturn(account);

        int result = service.purgeExpiredAuthorizations();
        assertEquals(1, result);
        verify(accountServiceClient).getAccount(1001L);
        verify(accountServiceClient).updateAccount(eq(1001L), any(AccountDto.class));
    }

    @Test
    void purgeExpiredAuthorizations_withNullMatchStatus_noAdjustment() {
        Authorization auth = Authorization.builder()
                .authId(1)
                .cardNum("4111111111111111")
                .authTs(LocalDateTime.now().minusDays(100))
                .matchStatus(null)
                .approvedAmt(new BigDecimal("100.00"))
                .acctId(1001L)
                .build();

        when(authorizationRepository.findByAuthTsBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(auth));

        int result = service.purgeExpiredAuthorizations();
        assertEquals(1, result);
        verify(accountServiceClient, never()).getAccount(any());
    }

    @Test
    void purgeExpiredAuthorizations_accountServiceFailure_continuesPurge() {
        Authorization auth = Authorization.builder()
                .authId(1)
                .cardNum("4111111111111111")
                .authTs(LocalDateTime.now().minusDays(100))
                .matchStatus("U")
                .approvedAmt(new BigDecimal("100.00"))
                .acctId(1001L)
                .build();

        when(authorizationRepository.findByAuthTsBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(auth));
        when(accountServiceClient.getAccount(1001L)).thenThrow(new RuntimeException("Service unavailable"));

        int result = service.purgeExpiredAuthorizations();
        assertEquals(1, result);
        verify(authorizationRepository).deleteAll(List.of(auth));
    }
}
