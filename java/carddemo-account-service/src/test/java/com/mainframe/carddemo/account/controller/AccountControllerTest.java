package com.mainframe.carddemo.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainframe.carddemo.account.service.AccountService;
import com.mainframe.carddemo.account.service.ResourceNotFoundException;
import com.mainframe.carddemo.common.dto.AccountDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @Test
    void getAccount_shouldReturnAccount() throws Exception {
        AccountDto dto = buildAccountDto();
        when(accountService.getAccountById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.activeStatus").value("Y"))
                .andExpect(jsonPath("$.currentBalance").value(5000.00))
                .andExpect(jsonPath("$.creditLimit").value(10000.00));
    }

    @Test
    void getAccount_notFound_shouldReturn404() throws Exception {
        when(accountService.getAccountById(999L))
                .thenThrow(new ResourceNotFoundException("Account not found: 999"));

        mockMvc.perform(get("/api/accounts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAccount_shouldReturnUpdated() throws Exception {
        AccountDto input = new AccountDto();
        input.setActiveStatus("N");
        input.setCreditLimit(new BigDecimal("15000.00"));

        AccountDto result = buildAccountDto();
        result.setActiveStatus("N");
        result.setCreditLimit(new BigDecimal("15000.00"));
        when(accountService.updateAccount(eq(1L), any(AccountDto.class))).thenReturn(result);

        mockMvc.perform(put("/api/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeStatus").value("N"))
                .andExpect(jsonPath("$.creditLimit").value(15000.00));
    }

    private AccountDto buildAccountDto() {
        AccountDto dto = new AccountDto();
        dto.setAccountId(1L);
        dto.setActiveStatus("Y");
        dto.setCurrentBalance(new BigDecimal("5000.00"));
        dto.setCreditLimit(new BigDecimal("10000.00"));
        dto.setCashCreditLimit(new BigDecimal("3000.00"));
        dto.setOpenDate(LocalDate.of(2020, 1, 15));
        dto.setExpirationDate(LocalDate.of(2025, 12, 31));
        dto.setAddressZip("10001");
        dto.setGroupId("GRP01");
        return dto;
    }
}
