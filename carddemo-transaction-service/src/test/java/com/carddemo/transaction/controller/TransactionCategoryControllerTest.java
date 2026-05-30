package com.carddemo.transaction.controller;

import com.carddemo.common.dto.TransactionCategoryDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.transaction.exception.GlobalExceptionHandler;
import com.carddemo.transaction.service.TransactionCategoryService;
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

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionCategoryController.class)
@Import(GlobalExceptionHandler.class)
class TransactionCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionCategoryService transactionCategoryService;

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
    void listTransactionCategories() throws Exception {
        TransactionCategoryDto dto = TransactionCategoryDto.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranCatTypeDesc("Retail")
                .build();

        when(transactionCategoryService.listTransactionCategories()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/transaction-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tranTypeCd").value("01"))
                .andExpect(jsonPath("$[0].tranCatCd").value(1));
    }

    @Test
    void getTransactionCategory() throws Exception {
        TransactionCategoryDto dto = TransactionCategoryDto.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranCatTypeDesc("Retail")
                .build();

        when(transactionCategoryService.getTransactionCategory("01", 1)).thenReturn(dto);

        mockMvc.perform(get("/api/transaction-categories/01/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranCatTypeDesc").value("Retail"));
    }

    @Test
    void getTransactionCategory_notFound() throws Exception {
        when(transactionCategoryService.getTransactionCategory("99", 99))
                .thenThrow(new ResourceNotFoundException("Transaction category not found: 99/99"));

        mockMvc.perform(get("/api/transaction-categories/99/99"))
                .andExpect(status().isNotFound());
    }
}
