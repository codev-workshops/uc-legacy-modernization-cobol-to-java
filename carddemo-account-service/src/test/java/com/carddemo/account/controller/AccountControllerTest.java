package com.carddemo.account.controller;

import com.carddemo.account.exception.GlobalExceptionHandler;
import com.carddemo.account.service.AccountService;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.model.PagedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
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

@WebMvcTest(AccountController.class)
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Test
    void listAccounts() throws Exception {
        AccountDto dto = AccountDto.builder().acctId(1L).acctActiveStatus("Y").build();
        PagedResponse<AccountDto> response = PagedResponse.<AccountDto>builder()
                .content(List.of(dto)).page(0).size(20).totalElements(1).totalPages(1).build();
        when(accountService.listAccounts(0, 20)).thenReturn(response);

        mockMvc.perform(get("/api/accounts").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].acctId").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAccount_found() throws Exception {
        AccountDto dto = AccountDto.builder().acctId(1L).acctActiveStatus("Y").build();
        when(accountService.getAccount(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acctId").value(1));
    }

    @Test
    void getAccount_notFound() throws Exception {
        when(accountService.getAccount(999L)).thenThrow(new ResourceNotFoundException("Account not found with ID: 999"));

        mockMvc.perform(get("/api/accounts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found with ID: 999"));
    }

    @Test
    void updateAccount_success() throws Exception {
        AccountDto dto = AccountDto.builder()
                .acctId(1L).acctActiveStatus("Y").acctCurrBal(new BigDecimal("2000.00")).build();
        when(accountService.updateAccount(eq(1L), any(AccountDto.class))).thenReturn(dto);

        mockMvc.perform(put("/api/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acctId").value(1));
    }

    @Test
    void updateAccount_notFound() throws Exception {
        AccountDto dto = AccountDto.builder().acctId(999L).build();
        when(accountService.updateAccount(eq(999L), any(AccountDto.class)))
                .thenThrow(new ResourceNotFoundException("Account not found with ID: 999"));

        mockMvc.perform(put("/api/accounts/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }
}
