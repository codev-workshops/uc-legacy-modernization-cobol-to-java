package com.carddemo.account.controller;

import com.carddemo.account.dto.BillingDto;
import com.carddemo.account.exception.GlobalExceptionHandler;
import com.carddemo.account.service.BillingService;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BillingController.class)
@Import(GlobalExceptionHandler.class)
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillingService billingService;

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
    void getAccountBilling_success() throws Exception {
        BillingDto dto = new BillingDto(
                1L,
                new BigDecimal("1000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("1500.00"),
                new BigDecimal("200.00"),
                new BigDecimal("100.00"),
                new BigDecimal("4000.00"),
                "2024-01-01",
                "2027-01-01"
        );
        when(billingService.getAccountBilling(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/accounts/1/billing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acctId").value(1))
                .andExpect(jsonPath("$.currentBalance").value(1000.00))
                .andExpect(jsonPath("$.availableCredit").value(4000.00));
    }

    @Test
    void getAccountBilling_notFound() throws Exception {
        when(billingService.getAccountBilling(999L))
                .thenThrow(new ResourceNotFoundException("Account not found with ID: 999"));

        mockMvc.perform(get("/api/accounts/999/billing"))
                .andExpect(status().isNotFound());
    }
}
