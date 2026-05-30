package com.carddemo.transaction.controller;

import com.carddemo.common.dto.TransactionDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.model.PagedResponse;
import com.carddemo.transaction.exception.GlobalExceptionHandler;
import com.carddemo.transaction.service.TransactionService;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

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
    void listTransactions() throws Exception {
        TransactionDto dto = TransactionDto.builder()
                .tranId("TX001")
                .tranAmt(new BigDecimal("100.00"))
                .build();
        PagedResponse<TransactionDto> pagedResponse = PagedResponse.<TransactionDto>builder()
                .content(List.of(dto))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(transactionService.listTransactions(anyInt(), anyInt())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/transactions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tranId").value("TX001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getTransaction() throws Exception {
        TransactionDto dto = TransactionDto.builder()
                .tranId("TX001")
                .tranAmt(new BigDecimal("100.00"))
                .build();

        when(transactionService.getTransaction("TX001")).thenReturn(dto);

        mockMvc.perform(get("/api/transactions/TX001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranId").value("TX001"));
    }

    @Test
    void getTransaction_notFound() throws Exception {
        when(transactionService.getTransaction("NOPE"))
                .thenThrow(new ResourceNotFoundException("Transaction not found: NOPE"));

        mockMvc.perform(get("/api/transactions/NOPE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found: NOPE"));
    }

    @Test
    void createTransaction() throws Exception {
        TransactionDto input = TransactionDto.builder()
                .tranCardNum("4111111111111111")
                .tranTypeCd("01")
                .tranAmt(new BigDecimal("50.00"))
                .build();
        TransactionDto output = TransactionDto.builder()
                .tranId("TX001")
                .tranCardNum("4111111111111111")
                .tranTypeCd("01")
                .tranAmt(new BigDecimal("50.00"))
                .build();

        when(transactionService.createTransaction(any(TransactionDto.class))).thenReturn(output);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tranId").value("TX001"));
    }
}
