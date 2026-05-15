package com.carddemo.controller;

import com.carddemo.model.Account;
import com.carddemo.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    private Account createTestAccount() {
        Account a = new Account();
        a.setAccountId(1L);
        a.setActiveStatus("Y");
        a.setCurrentBalance(new BigDecimal("1940.00"));
        a.setCreditLimit(new BigDecimal("20200.00"));
        a.setOpenDate(LocalDate.of(2014, 11, 20));
        return a;
    }

    @Test
    void getAll_returnsAccounts() throws Exception {
        when(accountService.findAll()).thenReturn(List.of(createTestAccount()));
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value(1))
                .andExpect(jsonPath("$[0].activeStatus").value("Y"));
    }

    @Test
    void getById_existingAccount_returnsOk() throws Exception {
        when(accountService.findById(1L)).thenReturn(Optional.of(createTestAccount()));
        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.currentBalance").value(1940.00));
    }

    @Test
    void getById_nonExistingAccount_returns404() throws Exception {
        when(accountService.findById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/accounts/999"))
                .andExpect(status().isNotFound());
    }
}
