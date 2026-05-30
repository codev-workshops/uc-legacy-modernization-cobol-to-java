package com.mainframe.carddemo.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainframe.carddemo.account.service.CardService;
import com.mainframe.carddemo.account.service.ResourceNotFoundException;
import com.mainframe.carddemo.common.dto.CardDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @Test
    void getCardsByAccount_shouldReturnList() throws Exception {
        CardDto card1 = buildCardDto("4111111111111111");
        CardDto card2 = buildCardDto("4111111111112222");
        when(cardService.getCardsByAccountId(1L)).thenReturn(List.of(card1, card2));

        mockMvc.perform(get("/api/cards").param("acctId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].cardNum").value("4111111111111111"));
    }

    @Test
    void getCard_shouldReturnCard() throws Exception {
        CardDto dto = buildCardDto("4111111111111111");
        when(cardService.getCardByNum("4111111111111111")).thenReturn(dto);

        mockMvc.perform(get("/api/cards/4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNum").value("4111111111111111"))
                .andExpect(jsonPath("$.embossedName").value("JOHN DOE"));
    }

    @Test
    void getCard_notFound_shouldReturn404() throws Exception {
        when(cardService.getCardByNum("0000000000000000"))
                .thenThrow(new ResourceNotFoundException("Card not found: 0000000000000000"));

        mockMvc.perform(get("/api/cards/0000000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCard_shouldReturnUpdated() throws Exception {
        CardDto input = new CardDto();
        input.setEmbossedName("JANE DOE");
        input.setActiveStatus("N");

        CardDto result = buildCardDto("4111111111111111");
        result.setEmbossedName("JANE DOE");
        result.setActiveStatus("N");
        when(cardService.updateCard(eq("4111111111111111"), any(CardDto.class))).thenReturn(result);

        mockMvc.perform(put("/api/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.embossedName").value("JANE DOE"))
                .andExpect(jsonPath("$.activeStatus").value("N"));
    }

    private CardDto buildCardDto(String num) {
        CardDto dto = new CardDto();
        dto.setCardNum(num);
        dto.setAccountId(1L);
        dto.setCvvCode(123);
        dto.setEmbossedName("JOHN DOE");
        dto.setExpirationDate(LocalDate.of(2026, 6, 30));
        dto.setActiveStatus("Y");
        return dto;
    }
}
