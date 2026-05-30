package com.carddemo.transaction.controller;

import com.carddemo.common.dto.TransactionTypeDto;
import com.carddemo.common.exception.DuplicateResourceException;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.transaction.exception.GlobalExceptionHandler;
import com.carddemo.transaction.service.TransactionTypeService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionTypeController.class)
@Import(GlobalExceptionHandler.class)
class TransactionTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionTypeService transactionTypeService;

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
    void listTransactionTypes() throws Exception {
        TransactionTypeDto dto = TransactionTypeDto.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .build();

        when(transactionTypeService.listTransactionTypes()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/transaction-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tranType").value("01"));
    }

    @Test
    void getTransactionType() throws Exception {
        TransactionTypeDto dto = TransactionTypeDto.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .build();

        when(transactionTypeService.getTransactionType("01")).thenReturn(dto);

        mockMvc.perform(get("/api/transaction-types/01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranType").value("01"));
    }

    @Test
    void getTransactionType_notFound() throws Exception {
        when(transactionTypeService.getTransactionType("99"))
                .thenThrow(new ResourceNotFoundException("Transaction type not found: 99"));

        mockMvc.perform(get("/api/transaction-types/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTransactionType() throws Exception {
        TransactionTypeDto dto = TransactionTypeDto.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .build();

        when(transactionTypeService.createTransactionType(any())).thenReturn(dto);

        mockMvc.perform(post("/api/transaction-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tranType").value("01"));
    }

    @Test
    void createTransactionType_duplicate() throws Exception {
        TransactionTypeDto dto = TransactionTypeDto.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .build();

        when(transactionTypeService.createTransactionType(any()))
                .thenThrow(new DuplicateResourceException("Transaction type already exists: 01"));

        mockMvc.perform(post("/api/transaction-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateTransactionType() throws Exception {
        TransactionTypeDto dto = TransactionTypeDto.builder()
                .tranType("01")
                .tranTypeDesc("Updated Purchase")
                .build();

        when(transactionTypeService.updateTransactionType(eq("01"), any())).thenReturn(dto);

        mockMvc.perform(put("/api/transaction-types/01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranTypeDesc").value("Updated Purchase"));
    }

    @Test
    void deleteTransactionType() throws Exception {
        doNothing().when(transactionTypeService).deleteTransactionType("01");

        mockMvc.perform(delete("/api/transaction-types/01"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTransactionType_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Transaction type not found: 99"))
                .when(transactionTypeService).deleteTransactionType("99");

        mockMvc.perform(delete("/api/transaction-types/99"))
                .andExpect(status().isNotFound());
    }
}
