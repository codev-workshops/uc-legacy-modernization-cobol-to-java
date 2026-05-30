package com.carddemo.online.controller;

import com.carddemo.online.config.SecurityConfig;
import com.carddemo.online.dto.CardDetailResponse;
import com.carddemo.online.dto.CardResponse;
import com.carddemo.online.dto.CardUpdateRequest;
import com.carddemo.online.security.JwtAuthenticationFilter;
import com.carddemo.online.security.JwtUtil;
import com.carddemo.online.service.CardService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CardController.class,
        excludeAutoConfiguration = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER")
    void listCards_authenticated_returnsOk() throws Exception {
        CardResponse card = new CardResponse("4111111111111111", 1001L, 123,
                "JOHN DOE", "2025-12-31", "Y");
        Page<CardResponse> page = new PageImpl<>(List.of(card), PageRequest.of(0, 20), 1);
        when(cardService.listCards(any())).thenReturn(page);

        mockMvc.perform(get("/api/cards").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardNum").value("4111111111111111"))
                .andExpect(jsonPath("$.content[0].acctId").value(1001))
                .andExpect(jsonPath("$.content[0].embossedName").value("JOHN DOE"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listCards_asAdmin_returnsOk() throws Exception {
        Page<CardResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(cardService.listCards(any())).thenReturn(page);

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void listCards_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCard_found_returnsOk() throws Exception {
        CardDetailResponse detail = new CardDetailResponse();
        detail.setCardNum("4111111111111111");
        detail.setAcctId(1001L);
        detail.setCvvCd(123);
        detail.setEmbossedName("JOHN DOE");
        detail.setExpirationDate("2025-12-31");
        detail.setActiveStatus("Y");
        detail.setAccount(new CardDetailResponse.AccountSummary(
                1001L, "Y", new BigDecimal("5000.00"), new BigDecimal("10000.00"), "2026-12-31"));
        detail.setCustomer(new CardDetailResponse.CustomerSummary(
                5001L, "John", "Doe"));

        when(cardService.getCard("4111111111111111")).thenReturn(detail);

        mockMvc.perform(get("/api/cards/4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNum").value("4111111111111111"))
                .andExpect(jsonPath("$.account.acctId").value(1001))
                .andExpect(jsonPath("$.account.currBal").value(5000.00))
                .andExpect(jsonPath("$.customer.custId").value(5001))
                .andExpect(jsonPath("$.customer.firstName").value("John"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCard_notFound_returns404() throws Exception {
        when(cardService.getCard("9999999999999999"))
                .thenThrow(new CardService.CardNotFoundException("Card not found: 9999999999999999"));

        mockMvc.perform(get("/api/cards/9999999999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Card not found: 9999999999999999"));
    }

    @Test
    void getCard_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/cards/4111111111111111"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCard_authenticated_returnsOk() throws Exception {
        CardUpdateRequest request = new CardUpdateRequest();
        request.setEmbossedName("JANE DOE");
        request.setActiveStatus("Y");

        CardResponse response = new CardResponse("4111111111111111", 1001L, 123,
                "JANE DOE", "2025-12-31", "Y");
        when(cardService.updateCard(eq("4111111111111111"), any(CardUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.embossedName").value("JANE DOE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCard_notFound_returns404() throws Exception {
        CardUpdateRequest request = new CardUpdateRequest();
        request.setEmbossedName("NOBODY");

        when(cardService.updateCard(eq("9999999999999999"), any(CardUpdateRequest.class)))
                .thenThrow(new CardService.CardNotFoundException("Card not found: 9999999999999999"));

        mockMvc.perform(put("/api/cards/9999999999999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCard_invalidActiveStatus_returns400() throws Exception {
        CardUpdateRequest request = new CardUpdateRequest();
        request.setActiveStatus("X");

        mockMvc.perform(put("/api/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCard_unauthenticated_returns401() throws Exception {
        CardUpdateRequest request = new CardUpdateRequest();
        request.setEmbossedName("TEST");

        mockMvc.perform(put("/api/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
