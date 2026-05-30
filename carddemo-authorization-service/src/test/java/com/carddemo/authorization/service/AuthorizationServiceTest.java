package com.carddemo.authorization.service;

import com.carddemo.authorization.dto.AuthorizationDto;
import com.carddemo.authorization.entity.AuthFraud;
import com.carddemo.authorization.entity.Authorization;
import com.carddemo.authorization.mapper.AuthorizationMapper;
import com.carddemo.authorization.repository.AuthFraudRepository;
import com.carddemo.authorization.repository.AuthorizationRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private AuthorizationRepository authorizationRepository;
    @Mock
    private AuthFraudRepository authFraudRepository;
    @Mock
    private AuthorizationMapper authorizationMapper;

    private AuthorizationService service;

    private Authorization sampleAuth;
    private AuthorizationDto sampleDto;

    @BeforeEach
    void setUp() {
        service = new AuthorizationService(authorizationRepository, authFraudRepository, authorizationMapper);

        sampleAuth = Authorization.builder()
                .authId(1)
                .cardNum("4111111111111111")
                .authTs(LocalDateTime.of(2025, 1, 15, 10, 30))
                .authType("SALE")
                .authIdCode("ABC123")
                .authRespCode("00")
                .authRespReason("APRV")
                .transactionAmt(new BigDecimal("150.00"))
                .approvedAmt(new BigDecimal("150.00"))
                .acctId(1001L)
                .custId(2001L)
                .merchantId("MERCH001")
                .merchantName("Test Merchant")
                .build();

        sampleDto = AuthorizationDto.builder()
                .authId(1)
                .cardNum("4111111111111111")
                .authRespCode("00")
                .transactionAmt(new BigDecimal("150.00"))
                .build();
    }

    @Test
    void getAuthorizationSummary_returnsListOfDtos() {
        when(authorizationRepository.findByCardNum("4111111111111111")).thenReturn(List.of(sampleAuth));
        when(authorizationMapper.toDtoList(List.of(sampleAuth))).thenReturn(List.of(sampleDto));

        List<AuthorizationDto> result = service.getAuthorizationSummary("4111111111111111");
        assertEquals(1, result.size());
        assertEquals("4111111111111111", result.get(0).getCardNum());
    }

    @Test
    void getAuthorizationDetail_found() {
        when(authorizationRepository.findById(1)).thenReturn(Optional.of(sampleAuth));
        when(authorizationMapper.toDto(sampleAuth)).thenReturn(sampleDto);

        AuthorizationDto result = service.getAuthorizationDetail(1);
        assertNotNull(result);
        assertEquals(1, result.getAuthId());
    }

    @Test
    void getAuthorizationDetail_notFound() {
        when(authorizationRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getAuthorizationDetail(999));
    }

    @Test
    void markAsFraud_copiesDataToFraudTable() {
        when(authorizationRepository.findById(1)).thenReturn(Optional.of(sampleAuth));
        when(authorizationMapper.toDto(sampleAuth)).thenReturn(sampleDto);
        when(authFraudRepository.save(any(AuthFraud.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthorizationDto result = service.markAsFraud(1);
        assertNotNull(result);

        ArgumentCaptor<AuthFraud> fraudCaptor = ArgumentCaptor.forClass(AuthFraud.class);
        verify(authFraudRepository).save(fraudCaptor.capture());
        AuthFraud fraud = fraudCaptor.getValue();
        assertEquals("4111111111111111", fraud.getCardNum());
        assertEquals("Y", fraud.getAuthFraudFlag());
        assertNotNull(fraud.getFraudRptDate());
        assertEquals(1001L, fraud.getAcctId());
    }

    @Test
    void markAsFraud_notFound() {
        when(authorizationRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.markAsFraud(999));
    }

    @Test
    void saveAuthorization_delegatesToRepository() {
        when(authorizationRepository.save(sampleAuth)).thenReturn(sampleAuth);

        Authorization result = service.saveAuthorization(sampleAuth);
        assertEquals(sampleAuth, result);
        verify(authorizationRepository).save(sampleAuth);
    }
}
