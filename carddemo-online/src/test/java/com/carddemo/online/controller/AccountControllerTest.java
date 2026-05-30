package com.carddemo.online.controller;

import com.carddemo.online.config.SecurityConfig;
import com.carddemo.online.dto.AccountResponse;
import com.carddemo.online.dto.AccountUpdateRequest;
import com.carddemo.online.security.JwtAuthenticationFilter;
import com.carddemo.online.security.JwtUtil;
import com.carddemo.online.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AccountController.class,
        excludeAutoConfiguration = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private AccountResponse sampleAccount() {
        AccountResponse resp = new AccountResponse();
        resp.setAcctId(1000000001L);
        resp.setActiveStatus("Y");
        resp.setCurrBal(new BigDecimal("1500.00"));
        resp.setCreditLimit(new BigDecimal("5000.00"));
        resp.setCashCreditLimit(new BigDecimal("1500.00"));
        resp.setOpenDate("2020-01-15");
        resp.setExpirationDate("2025-01-15");
        resp.setAddrZip("60601");
        resp.setCards(List.of(
                new AccountResponse.CardSummary("4111111111111111", "JOHN DOE", "Y", "2025-01-15")));
        resp.setCustomer(new AccountResponse.CustomerSummary(100001L, "John", "Doe"));
        return resp;
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAccount_returnsOk() throws Exception {
        when(accountService.getAccount(1000000001L)).thenReturn(sampleAccount());

        mockMvc.perform(get("/api/accounts/1000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acctId").value(1000000001L))
                .andExpect(jsonPath("$.activeStatus").value("Y"))
                .andExpect(jsonPath("$.currBal").value(1500.00))
                .andExpect(jsonPath("$.cards[0].cardNum").value("4111111111111111"))
                .andExpect(jsonPath("$.customer.custId").value(100001));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAccount_notFound_returns404() throws Exception {
        when(accountService.getAccount(999L))
                .thenThrow(new AccountService.AccountNotFoundException("Account not found: 999"));

        mockMvc.perform(get("/api/accounts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account not found: 999"));
    }

    @Test
    void getAccount_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/accounts/1000000001"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateAccount_returnsOk() throws Exception {
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setActiveStatus("N");
        request.setCreditLimit(new BigDecimal("10000.00"));

        AccountResponse updated = sampleAccount();
        updated.setActiveStatus("N");
        updated.setCreditLimit(new BigDecimal("10000.00"));

        when(accountService.updateAccount(eq(1000000001L), any(AccountUpdateRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/accounts/1000000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeStatus").value("N"))
                .andExpect(jsonPath("$.creditLimit").value(10000.00));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateAccount_notFound_returns404() throws Exception {
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setActiveStatus("Y");

        when(accountService.updateAccount(eq(999L), any(AccountUpdateRequest.class)))
                .thenThrow(new AccountService.AccountNotFoundException("Account not found: 999"));

        mockMvc.perform(put("/api/accounts/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateAccount_invalidStatus_returns400() throws Exception {
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setActiveStatus("X");

        mockMvc.perform(put("/api/accounts/1000000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void listAccounts_returnsPage() throws Exception {
        AccountResponse resp = sampleAccount();
        Page<AccountResponse> page = new PageImpl<>(List.of(resp));

        when(accountService.listAccounts(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].acctId").value(1000000001L))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listAccounts_withStatusFilter_returnsPage() throws Exception {
        AccountResponse resp = sampleAccount();
        Page<AccountResponse> page = new PageImpl<>(List.of(resp));

        when(accountService.listAccounts(eq("Y"), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/accounts").param("status", "Y"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].activeStatus").value("Y"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listAccounts_withCustomerIdFilter_returnsPage() throws Exception {
        AccountResponse resp = sampleAccount();
        Page<AccountResponse> page = new PageImpl<>(List.of(resp));

        when(accountService.listAccounts(isNull(), eq(100001L), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/accounts").param("customerId", "100001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customer.custId").value(100001));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listAccounts_asAdmin_returnsOk() throws Exception {
        Page<AccountResponse> page = new PageImpl<>(List.of());
        when(accountService.listAccounts(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk());
    }
}
