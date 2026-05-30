package com.carddemo.account.controller;

import com.carddemo.account.exception.GlobalExceptionHandler;
import com.carddemo.account.service.CardService;
import com.carddemo.common.dto.CardDto;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
@Import(GlobalExceptionHandler.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

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
    void listCards() throws Exception {
        CardDto dto = CardDto.builder().cardNum("4111111111111111").cardAcctId(1L).build();
        PagedResponse<CardDto> response = PagedResponse.<CardDto>builder()
                .content(List.of(dto)).page(0).size(20).totalElements(1).totalPages(1).build();
        when(cardService.listCards(0, 20)).thenReturn(response);

        mockMvc.perform(get("/api/cards").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardNum").value("4111111111111111"));
    }

    @Test
    void getCard_found() throws Exception {
        CardDto dto = CardDto.builder().cardNum("4111111111111111").cardAcctId(1L).build();
        when(cardService.getCard("4111111111111111")).thenReturn(dto);

        mockMvc.perform(get("/api/cards/4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNum").value("4111111111111111"));
    }

    @Test
    void getCard_notFound() throws Exception {
        when(cardService.getCard("0000000000000000"))
                .thenThrow(new ResourceNotFoundException("Card not found"));

        mockMvc.perform(get("/api/cards/0000000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCard_success() throws Exception {
        CardDto dto = CardDto.builder().cardNum("4111111111111111").cardAcctId(1L).cardActiveStatus("Y").build();
        when(cardService.updateCard(eq("4111111111111111"), any(CardDto.class))).thenReturn(dto);

        mockMvc.perform(put("/api/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNum").value("4111111111111111"));
    }

    @Test
    void updateCard_notFound() throws Exception {
        CardDto dto = CardDto.builder().cardNum("0000000000000000").cardAcctId(1L).build();
        when(cardService.updateCard(eq("0000000000000000"), any(CardDto.class)))
                .thenThrow(new ResourceNotFoundException("Card not found"));

        mockMvc.perform(put("/api/cards/0000000000000000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCardsByAccount() throws Exception {
        CardDto dto = CardDto.builder().cardNum("4111111111111111").cardAcctId(1L).build();
        when(cardService.getCardsByAccount(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/cards/by-account/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cardNum").value("4111111111111111"));
    }
}
