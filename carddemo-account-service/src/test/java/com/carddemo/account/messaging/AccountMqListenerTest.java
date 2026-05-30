package com.carddemo.account.messaging;

import com.carddemo.account.service.AccountService;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountMqListenerTest {

    @Mock
    private AccountService accountService;
    @Mock
    private RabbitTemplate rabbitTemplate;

    private AccountMqListener listener;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        listener = new AccountMqListener(accountService, rabbitTemplate, objectMapper);
    }

    @Test
    void handleAccountInquiry_success() {
        AccountDto account = AccountDto.builder()
                .acctId(1001L)
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("500.00"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .build();
        when(accountService.getAccount(1001L)).thenReturn(account);

        listener.handleAccountInquiry("1001");

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
                eq(AccountMqListener.ACCOUNT_REPLY_QUEUE),
                responseCaptor.capture());
        String response = responseCaptor.getValue();
        assertTrue(response.contains("1001"));
        assertTrue(response.contains("500"));
    }

    @Test
    void handleAccountInquiry_notFound() {
        when(accountService.getAccount(9999L))
                .thenThrow(new ResourceNotFoundException("Account not found"));

        listener.handleAccountInquiry("9999");

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
                eq(AccountMqListener.ACCOUNT_REPLY_QUEUE),
                responseCaptor.capture());
        assertTrue(responseCaptor.getValue().contains("error"));
    }

    @Test
    void handleAccountInquiry_invalidInput() {
        listener.handleAccountInquiry("not-a-number");

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
                eq(AccountMqListener.ACCOUNT_REPLY_QUEUE),
                responseCaptor.capture());
        assertTrue(responseCaptor.getValue().contains("error"));
    }
}
