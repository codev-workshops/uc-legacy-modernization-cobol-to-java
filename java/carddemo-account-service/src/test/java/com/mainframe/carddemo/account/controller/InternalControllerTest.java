package com.mainframe.carddemo.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainframe.carddemo.account.service.AccountService;
import com.mainframe.carddemo.account.service.CardXrefService;
import com.mainframe.carddemo.common.dto.AccountDto;
import com.mainframe.carddemo.common.dto.BalanceUpdateDto;
import com.mainframe.carddemo.common.dto.CardXrefDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalController.class)
class InternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardXrefService cardXrefService;

    @MockBean
    private AccountService accountService;

    @Test
    void getXrefByCardNum_shouldReturnXref() throws Exception {
        CardXrefDto dto = new CardXrefDto();
        dto.setCardNum("4111111111111111");
        dto.setCustomerId(100L);
        dto.setAccountId(1L);
        when(cardXrefService.getByCardNum("4111111111111111")).thenReturn(dto);

        mockMvc.perform(get("/internal/xref/4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNum").value("4111111111111111"))
                .andExpect(jsonPath("$.customerId").value(100))
                .andExpect(jsonPath("$.accountId").value(1));
    }

    @Test
    void getXrefByAccountId_shouldReturnList() throws Exception {
        CardXrefDto dto = new CardXrefDto();
        dto.setCardNum("4111111111111111");
        dto.setCustomerId(100L);
        dto.setAccountId(1L);
        when(cardXrefService.getByAccountId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/internal/xref/byAccount/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].accountId").value(1));
    }

    @Test
    void getAccountInternal_shouldReturnAccount() throws Exception {
        AccountDto dto = new AccountDto();
        dto.setAccountId(1L);
        dto.setActiveStatus("Y");
        dto.setCurrentBalance(new BigDecimal("5000.00"));
        when(accountService.getAccountById(1L)).thenReturn(dto);

        mockMvc.perform(get("/internal/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.currentBalance").value(5000.00));
    }

    @Test
    void updateBalance_shouldReturnUpdated() throws Exception {
        BalanceUpdateDto input = new BalanceUpdateDto();
        input.setCurrentBalance(new BigDecimal("6000.00"));
        input.setCurrentCycleCredit(new BigDecimal("1000.00"));
        input.setCurrentCycleDebit(new BigDecimal("500.00"));

        AccountDto result = new AccountDto();
        result.setAccountId(1L);
        result.setCurrentBalance(new BigDecimal("6000.00"));
        result.setCurrentCycleCredit(new BigDecimal("1000.00"));
        result.setCurrentCycleDebit(new BigDecimal("500.00"));
        when(accountService.updateBalance(eq(1L), any(BalanceUpdateDto.class))).thenReturn(result);

        mockMvc.perform(put("/internal/accounts/1/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(6000.00))
                .andExpect(jsonPath("$.currentCycleCredit").value(1000.00))
                .andExpect(jsonPath("$.currentCycleDebit").value(500.00));
    }
}
