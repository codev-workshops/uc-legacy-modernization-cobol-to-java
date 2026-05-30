package com.carddemo.online.controller;

import com.carddemo.online.config.SecurityConfig;
import com.carddemo.online.dto.TransactionRequest;
import com.carddemo.online.dto.TransactionResponse;
import com.carddemo.online.security.JwtAuthenticationFilter;
import com.carddemo.online.security.JwtUtil;
import com.carddemo.online.service.TransactionService;
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
import org.springframework.data.domain.PageRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionController.class,
        excludeAutoConfiguration = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private TransactionResponse sampleResponse() {
        return new TransactionResponse(
                "TXN0000000000001", "SA", 5001, "ONLINE",
                "Test purchase", new BigDecimal("25.50"), 1001L,
                "ACME Store", "New York", "10001",
                "4111111111111111", "2026-01-15-10.30.00.000000",
                "2026-01-15-10.30.00.000000");
    }

    @Test
    @WithMockUser(roles = "USER")
    void listTransactions_authenticated_returnsOk() throws Exception {
        Page<TransactionResponse> page = new PageImpl<>(
                List.of(sampleResponse()), PageRequest.of(0, 20), 1);
        when(transactionService.listTransactions(isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tranId").value("TXN0000000000001"))
                .andExpect(jsonPath("$.content[0].amt").value(25.50))
                .andExpect(jsonPath("$.content[0].cardNum").value("4111111111111111"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listTransactions_withAccountFilter_returnsOk() throws Exception {
        Page<TransactionResponse> page = new PageImpl<>(
                List.of(sampleResponse()), PageRequest.of(0, 20), 1);
        when(transactionService.listTransactions(eq(100L), isNull(), isNull(), isNull(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/transactions").param("accountId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tranId").value("TXN0000000000001"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listTransactions_withCardFilter_returnsOk() throws Exception {
        Page<TransactionResponse> page = new PageImpl<>(
                List.of(sampleResponse()), PageRequest.of(0, 20), 1);
        when(transactionService.listTransactions(isNull(), eq("4111111111111111"), isNull(), isNull(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/transactions").param("cardNum", "4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardNum").value("4111111111111111"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listTransactions_withDateRange_returnsOk() throws Exception {
        Page<TransactionResponse> page = new PageImpl<>(
                List.of(sampleResponse()), PageRequest.of(0, 20), 1);
        when(transactionService.listTransactions(isNull(), isNull(),
                eq("2026-01-01"), eq("2026-12-31"), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/transactions")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void listTransactions_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTransaction_found_returnsOk() throws Exception {
        when(transactionService.getTransaction("TXN0000000000001"))
                .thenReturn(sampleResponse());

        mockMvc.perform(get("/api/transactions/TXN0000000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranId").value("TXN0000000000001"))
                .andExpect(jsonPath("$.amt").value(25.50));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTransaction_notFound_returns404() throws Exception {
        when(transactionService.getTransaction("MISSING"))
                .thenThrow(new TransactionService.TransactionNotFoundException(
                        "Transaction not found: MISSING"));

        mockMvc.perform(get("/api/transactions/MISSING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Transaction not found: MISSING"));
    }

    @Test
    void getTransaction_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/transactions/TXN0000000000001"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void addTransaction_valid_returns201() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setCardNum("4111111111111111");
        request.setAmt(new BigDecimal("25.50"));
        request.setTypeCd("SA");
        request.setDescription("Test purchase");

        when(transactionService.addTransaction(any(TransactionRequest.class)))
                .thenReturn(sampleResponse());

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tranId").value("TXN0000000000001"))
                .andExpect(jsonPath("$.amt").value(25.50));
    }

    @Test
    @WithMockUser(roles = "USER")
    void addTransaction_overlimit_returns422() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setCardNum("4111111111111111");
        request.setAmt(new BigDecimal("99999.99"));

        when(transactionService.addTransaction(any(TransactionRequest.class)))
                .thenThrow(new TransactionService.TransactionRejectedException(
                        102, "OVERLIMIT TRANSACTION"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("OVERLIMIT TRANSACTION"))
                .andExpect(jsonPath("$.reasonCode").value(102));
    }

    @Test
    @WithMockUser(roles = "USER")
    void addTransaction_expiredCard_returns422() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setCardNum("4111111111111111");
        request.setAmt(new BigDecimal("10.00"));

        when(transactionService.addTransaction(any(TransactionRequest.class)))
                .thenThrow(new TransactionService.TransactionRejectedException(
                        103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("TRANSACTION RECEIVED AFTER ACCT EXPIRATION"))
                .andExpect(jsonPath("$.reasonCode").value(103));
    }

    @Test
    @WithMockUser(roles = "USER")
    void addTransaction_missingCardNum_returns400() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAmt(new BigDecimal("10.00"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void addTransaction_missingAmt_returns400() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setCardNum("4111111111111111");

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTransaction_unauthenticated_returns401() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setCardNum("4111111111111111");
        request.setAmt(new BigDecimal("10.00"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addTransaction_asAdmin_returns201() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setCardNum("4111111111111111");
        request.setAmt(new BigDecimal("25.50"));
        request.setTypeCd("SA");

        when(transactionService.addTransaction(any(TransactionRequest.class)))
                .thenReturn(sampleResponse());

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
