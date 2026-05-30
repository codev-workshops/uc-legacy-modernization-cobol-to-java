package com.carddemo.account.controller;

import com.carddemo.account.exception.GlobalExceptionHandler;
import com.carddemo.account.service.CardXrefService;
import com.carddemo.common.dto.CardXrefDto;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardXrefController.class)
@Import(GlobalExceptionHandler.class)
class CardXrefControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardXrefService cardXrefService;

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
    void getCardXref_found() throws Exception {
        CardXrefDto dto = CardXrefDto.builder()
                .xrefCardNum("4111111111111111")
                .xrefCustId(100L)
                .xrefAcctId(1L)
                .build();
        when(cardXrefService.getCardXref("4111111111111111")).thenReturn(dto);

        mockMvc.perform(get("/api/card-xref/4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.xrefCardNum").value("4111111111111111"))
                .andExpect(jsonPath("$.xrefAcctId").value(1));
    }

    @Test
    void getCardXref_notFound() throws Exception {
        when(cardXrefService.getCardXref("0000000000000000"))
                .thenThrow(new ResourceNotFoundException("Card xref not found"));

        mockMvc.perform(get("/api/card-xref/0000000000000000"))
                .andExpect(status().isNotFound());
    }
}
