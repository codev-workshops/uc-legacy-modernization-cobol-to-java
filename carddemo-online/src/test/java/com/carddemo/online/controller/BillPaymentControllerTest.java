package com.carddemo.online.controller;

import com.carddemo.online.config.SecurityConfig;
import com.carddemo.online.dto.BillPaymentRequest;
import com.carddemo.online.dto.BillPaymentResponse;
import com.carddemo.online.security.JwtAuthenticationFilter;
import com.carddemo.online.security.JwtUtil;
import com.carddemo.online.service.BillPaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BillPaymentController.class,
        excludeAutoConfiguration = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class BillPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillPaymentService billPaymentService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "USER01", roles = "USER")
    void payBill_success() throws Exception {
        BillPaymentResponse response = new BillPaymentResponse(
                "0000000000000001", 10001L, new BigDecimal("500.00"),
                new BigDecimal("500.00"), "Bill payment processed successfully");
        when(billPaymentService.processPayment(any(BillPaymentRequest.class)))
                .thenReturn(response);

        BillPaymentRequest request = new BillPaymentRequest(
                10001L, new BigDecimal("500.00"), "ONLINE");
        mockMvc.perform(post("/api/bills/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("0000000000000001"))
                .andExpect(jsonPath("$.accountId").value(10001))
                .andExpect(jsonPath("$.paymentAmount").value(500.00))
                .andExpect(jsonPath("$.newBalance").value(500.00))
                .andExpect(jsonPath("$.message").value("Bill payment processed successfully"));
    }

    @Test
    @WithMockUser(username = "ADMIN01", roles = "ADMIN")
    void payBill_adminCanAlsoAccess() throws Exception {
        BillPaymentResponse response = new BillPaymentResponse(
                "0000000000000002", 10001L, new BigDecimal("100.00"),
                new BigDecimal("900.00"), "Bill payment processed successfully");
        when(billPaymentService.processPayment(any(BillPaymentRequest.class)))
                .thenReturn(response);

        BillPaymentRequest request = new BillPaymentRequest(
                10001L, new BigDecimal("100.00"), "ONLINE");
        mockMvc.perform(post("/api/bills/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("0000000000000002"));
    }

    @Test
    void payBill_unauthenticated_returns401() throws Exception {
        BillPaymentRequest request = new BillPaymentRequest(
                10001L, new BigDecimal("500.00"), "ONLINE");
        mockMvc.perform(post("/api/bills/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "USER01", roles = "USER")
    void payBill_accountNotFound_returns404() throws Exception {
        when(billPaymentService.processPayment(any(BillPaymentRequest.class)))
                .thenThrow(new BillPaymentService.AccountNotFoundException("Account ID not found: 99999"));

        BillPaymentRequest request = new BillPaymentRequest(
                99999L, new BigDecimal("500.00"), "ONLINE");
        mockMvc.perform(post("/api/bills/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account ID not found: 99999"));
    }

    @Test
    @WithMockUser(username = "USER01", roles = "USER")
    void payBill_accountNotActive_returns400() throws Exception {
        when(billPaymentService.processPayment(any(BillPaymentRequest.class)))
                .thenThrow(new BillPaymentService.AccountNotActiveException("Account is not active: 10001"));

        BillPaymentRequest request = new BillPaymentRequest(
                10001L, new BigDecimal("500.00"), "ONLINE");
        mockMvc.perform(post("/api/bills/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Account is not active: 10001"));
    }

    @Test
    @WithMockUser(username = "USER01", roles = "USER")
    void payBill_invalidPayment_returns400() throws Exception {
        when(billPaymentService.processPayment(any(BillPaymentRequest.class)))
                .thenThrow(new BillPaymentService.InvalidPaymentException("Payment amount must be positive"));

        BillPaymentRequest request = new BillPaymentRequest(
                10001L, new BigDecimal("500.00"), "ONLINE");
        mockMvc.perform(post("/api/bills/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Payment amount must be positive"));
    }

    @Test
    @WithMockUser(username = "USER01", roles = "USER")
    void payBill_nullAccountId_returns400() throws Exception {
        BillPaymentRequest request = new BillPaymentRequest(
                null, new BigDecimal("500.00"), "ONLINE");
        mockMvc.perform(post("/api/bills/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "USER01", roles = "USER")
    void payBill_nullPaymentAmount_returns400() throws Exception {
        BillPaymentRequest request = new BillPaymentRequest(
                10001L, null, "ONLINE");
        mockMvc.perform(post("/api/bills/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "USER01", roles = "USER")
    void payBill_zeroPaymentAmount_returns400() throws Exception {
        BillPaymentRequest request = new BillPaymentRequest(
                10001L, BigDecimal.ZERO, "ONLINE");
        mockMvc.perform(post("/api/bills/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "USER01", roles = "USER")
    void payBill_negativePaymentAmount_returns400() throws Exception {
        BillPaymentRequest request = new BillPaymentRequest(
                10001L, new BigDecimal("-100.00"), "ONLINE");
        mockMvc.perform(post("/api/bills/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
