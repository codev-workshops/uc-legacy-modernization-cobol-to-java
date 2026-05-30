package com.carddemo.authorization.controller;

import com.carddemo.authorization.dto.AuthorizationDto;
import com.carddemo.authorization.config.SecurityConfig;
import com.carddemo.authorization.service.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthorizationController.class)
@Import(SecurityConfig.class)
class AuthorizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorizationService authorizationService;

    @Test
    void getAuthorizationSummary_returnsList() throws Exception {
        AuthorizationDto dto = AuthorizationDto.builder()
                .authId(1)
                .cardNum("4111111111111111")
                .authRespCode("00")
                .transactionAmt(new BigDecimal("150.00"))
                .authTs(LocalDateTime.of(2025, 1, 15, 10, 30))
                .build();

        when(authorizationService.getAuthorizationSummary("4111111111111111"))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/authorizations/summary")
                        .param("cardNum", "4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cardNum").value("4111111111111111"))
                .andExpect(jsonPath("$[0].authRespCode").value("00"));
    }

    @Test
    void getAuthorizationDetail_found() throws Exception {
        AuthorizationDto dto = AuthorizationDto.builder()
                .authId(1)
                .cardNum("4111111111111111")
                .authRespCode("00")
                .transactionAmt(new BigDecimal("150.00"))
                .build();

        when(authorizationService.getAuthorizationDetail(1)).thenReturn(dto);

        mockMvc.perform(get("/api/authorizations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authId").value(1))
                .andExpect(jsonPath("$.cardNum").value("4111111111111111"));
    }

    @Test
    void markAsFraud_success() throws Exception {
        AuthorizationDto dto = AuthorizationDto.builder()
                .authId(1)
                .cardNum("4111111111111111")
                .authRespCode("00")
                .build();

        when(authorizationService.markAsFraud(1)).thenReturn(dto);

        mockMvc.perform(post("/api/authorizations/1/mark-fraud"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authId").value(1));
    }
}
