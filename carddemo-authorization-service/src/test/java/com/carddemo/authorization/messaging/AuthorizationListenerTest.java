package com.carddemo.authorization.messaging;

import com.carddemo.authorization.entity.Authorization;
import com.carddemo.authorization.repository.AuthorizationRepository;
import com.carddemo.common.client.AccountServiceClient;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.dto.CardXrefDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationListenerTest {

    @Mock
    private AuthorizationRepository authorizationRepository;
    @Mock
    private AccountServiceClient accountServiceClient;
    @Mock
    private RabbitTemplate rabbitTemplate;

    private AuthorizationListener listener;

    private static final String VALID_CSV = "2025-01-15,10:30:00,4111111111111111,SALE,1226,100000,ONLINE," +
            "000000,150.00,5411,840,05,MERCH001,Test Merchant,New York,NY,10001,TXN001";

    @BeforeEach
    void setUp() {
        listener = new AuthorizationListener(authorizationRepository, accountServiceClient, rabbitTemplate);
    }

    @Test
    void parseCsvFields_validInput_returns18Fields() {
        String[] fields = listener.parseCsvFields(VALID_CSV);
        assertEquals(18, fields.length);
        assertEquals("2025-01-15", fields[0]);
        assertEquals("10:30:00", fields[1]);
        assertEquals("4111111111111111", fields[2]);
        assertEquals("SALE", fields[3]);
        assertEquals("1226", fields[4]);
        assertEquals("100000", fields[5]);
        assertEquals("ONLINE", fields[6]);
        assertEquals("000000", fields[7]);
        assertEquals("150.00", fields[8]);
        assertEquals("5411", fields[9]);
        assertEquals("840", fields[10]);
        assertEquals("05", fields[11]);
        assertEquals("MERCH001", fields[12]);
        assertEquals("Test Merchant", fields[13]);
        assertEquals("New York", fields[14]);
        assertEquals("NY", fields[15]);
        assertEquals("10001", fields[16]);
        assertEquals("TXN001", fields[17]);
    }

    @Test
    void parseCsvFields_malformedInput_returnsFewerFields() {
        String[] fields = listener.parseCsvFields("field1,field2");
        assertEquals(2, fields.length);
    }

    @Test
    void processAuthorizationRequest_approved() {
        CardXrefDto xref = CardXrefDto.builder()
                .xrefCardNum("4111111111111111")
                .xrefAcctId(1001L)
                .xrefCustId(2001L)
                .build();
        AccountDto account = AccountDto.builder()
                .acctId(1001L)
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("500.00"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .build();

        when(accountServiceClient.getCardXref("4111111111111111")).thenReturn(xref);
        when(accountServiceClient.getAccount(1001L)).thenReturn(account);
        when(authorizationRepository.save(any(Authorization.class))).thenAnswer(inv -> {
            Authorization a = inv.getArgument(0);
            a.setAuthId(1);
            return a;
        });

        listener.processAuthorizationRequest(VALID_CSV);

        ArgumentCaptor<Authorization> authCaptor = ArgumentCaptor.forClass(Authorization.class);
        verify(authorizationRepository).save(authCaptor.capture());
        Authorization saved = authCaptor.getValue();
        assertEquals("00", saved.getAuthRespCode());
        assertEquals(new BigDecimal("150.00"), saved.getApprovedAmt());
        assertEquals("4111111111111111", saved.getCardNum());
        assertEquals(1001L, saved.getAcctId());

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq("carddemo.auth.reply"), responseCaptor.capture());
        String response = responseCaptor.getValue();
        assertTrue(response.contains("4111111111111111"));
        assertTrue(response.contains("TXN001"));
        assertTrue(response.contains("00"));
        assertTrue(response.contains("150"));
    }

    @Test
    void processAuthorizationRequest_declinedExpiredCard() {
        String csvWithExpiredCard = "2025-01-15,10:30:00,4111111111111111,SALE,0120,100000,ONLINE," +
                "000000,150.00,5411,840,05,MERCH001,Test Merchant,New York,NY,10001,TXN001";

        CardXrefDto xref = CardXrefDto.builder()
                .xrefCardNum("4111111111111111")
                .xrefAcctId(1001L)
                .xrefCustId(2001L)
                .build();
        AccountDto account = AccountDto.builder()
                .acctId(1001L)
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("500.00"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .build();

        when(accountServiceClient.getCardXref("4111111111111111")).thenReturn(xref);
        when(accountServiceClient.getAccount(1001L)).thenReturn(account);
        when(authorizationRepository.save(any(Authorization.class))).thenAnswer(inv -> {
            Authorization a = inv.getArgument(0);
            a.setAuthId(1);
            return a;
        });

        listener.processAuthorizationRequest(csvWithExpiredCard);

        ArgumentCaptor<Authorization> authCaptor = ArgumentCaptor.forClass(Authorization.class);
        verify(authorizationRepository).save(authCaptor.capture());
        assertEquals("05", authCaptor.getValue().getAuthRespCode());
        assertEquals(BigDecimal.ZERO, authCaptor.getValue().getApprovedAmt());
    }

    @Test
    void processAuthorizationRequest_declinedInactiveAccount() {
        CardXrefDto xref = CardXrefDto.builder()
                .xrefCardNum("4111111111111111")
                .xrefAcctId(1001L)
                .xrefCustId(2001L)
                .build();
        AccountDto account = AccountDto.builder()
                .acctId(1001L)
                .acctActiveStatus("N")
                .acctCurrBal(new BigDecimal("500.00"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .build();

        when(accountServiceClient.getCardXref("4111111111111111")).thenReturn(xref);
        when(accountServiceClient.getAccount(1001L)).thenReturn(account);
        when(authorizationRepository.save(any(Authorization.class))).thenAnswer(inv -> {
            Authorization a = inv.getArgument(0);
            a.setAuthId(1);
            return a;
        });

        listener.processAuthorizationRequest(VALID_CSV);

        ArgumentCaptor<Authorization> authCaptor = ArgumentCaptor.forClass(Authorization.class);
        verify(authorizationRepository).save(authCaptor.capture());
        assertEquals("05", authCaptor.getValue().getAuthRespCode());
        assertEquals("INAC", authCaptor.getValue().getAuthRespReason());
    }

    @Test
    void processAuthorizationRequest_declinedOverCreditLimit() {
        String csvHighAmt = "2025-01-15,10:30:00,4111111111111111,SALE,1226,100000,ONLINE," +
                "000000,6000.00,5411,840,05,MERCH001,Test Merchant,New York,NY,10001,TXN001";

        CardXrefDto xref = CardXrefDto.builder()
                .xrefCardNum("4111111111111111")
                .xrefAcctId(1001L)
                .xrefCustId(2001L)
                .build();
        AccountDto account = AccountDto.builder()
                .acctId(1001L)
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("4500.00"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .build();

        when(accountServiceClient.getCardXref("4111111111111111")).thenReturn(xref);
        when(accountServiceClient.getAccount(1001L)).thenReturn(account);
        when(authorizationRepository.save(any(Authorization.class))).thenAnswer(inv -> {
            Authorization a = inv.getArgument(0);
            a.setAuthId(1);
            return a;
        });

        listener.processAuthorizationRequest(csvHighAmt);

        ArgumentCaptor<Authorization> authCaptor = ArgumentCaptor.forClass(Authorization.class);
        verify(authorizationRepository).save(authCaptor.capture());
        assertEquals("05", authCaptor.getValue().getAuthRespCode());
        assertEquals("OVLM", authCaptor.getValue().getAuthRespReason());
    }

    @Test
    void processAuthorizationRequest_malformedInput() {
        listener.processAuthorizationRequest("field1,field2");

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq("carddemo.auth.reply"), responseCaptor.capture());
        assertTrue(responseCaptor.getValue().contains("05"));
        assertTrue(responseCaptor.getValue().contains("MALF"));
    }

    @Test
    void isCardExpired_futureDate_returnsFalse() {
        assertFalse(listener.isCardExpired("1230"));
    }

    @Test
    void isCardExpired_pastDate_returnsTrue() {
        assertTrue(listener.isCardExpired("0120"));
    }

    @Test
    void isCardExpired_nullOrEmpty_returnsFalse() {
        assertFalse(listener.isCardExpired(null));
        assertFalse(listener.isCardExpired(""));
        assertFalse(listener.isCardExpired("   "));
    }

    @Test
    void isCardExpired_invalidFormat_returnsFalse() {
        assertFalse(listener.isCardExpired("ABCD"));
    }

    @Test
    void parseAuthTimestamp_validInput() {
        assertNotNull(listener.parseAuthTimestamp("2025-01-15", "10:30:00"));
    }

    @Test
    void parseAuthTimestamp_invalidInput_returnsCurrentTime() {
        assertNotNull(listener.parseAuthTimestamp("invalid", "time"));
    }

    @Test
    void generateAuthIdCode_returns6Chars() {
        String code = listener.generateAuthIdCode();
        assertEquals(6, code.length());
    }

    @Test
    void responseCsvFormat_containsAllFields() {
        CardXrefDto xref = CardXrefDto.builder()
                .xrefCardNum("4111111111111111")
                .xrefAcctId(1001L)
                .xrefCustId(2001L)
                .build();
        AccountDto account = AccountDto.builder()
                .acctId(1001L)
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("500.00"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .build();

        when(accountServiceClient.getCardXref("4111111111111111")).thenReturn(xref);
        when(accountServiceClient.getAccount(1001L)).thenReturn(account);
        when(authorizationRepository.save(any(Authorization.class))).thenAnswer(inv -> {
            Authorization a = inv.getArgument(0);
            a.setAuthId(1);
            return a;
        });

        listener.processAuthorizationRequest(VALID_CSV);

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq("carddemo.auth.reply"), responseCaptor.capture());
        String[] responseParts = responseCaptor.getValue().split(",");
        assertEquals(6, responseParts.length);
        assertEquals("4111111111111111", responseParts[0]);
        assertEquals("TXN001", responseParts[1]);
        assertEquals(6, responseParts[2].length());
        assertEquals("00", responseParts[3]);
        assertEquals("APRV", responseParts[4]);
    }
}
