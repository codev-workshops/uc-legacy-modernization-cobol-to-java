package com.carddemo.account.service;

import com.carddemo.account.entity.Card;
import com.carddemo.account.mapper.CardMapper;
import com.carddemo.account.repository.CardRepository;
import com.carddemo.common.dto.CardDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.model.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    private Card card;
    private CardDto cardDto;

    @BeforeEach
    void setUp() {
        card = Card.builder()
                .cardNum("4111111111111111")
                .cardAcctId(1L)
                .cardCvvCd(123)
                .cardEmbossedName("John Doe")
                .cardExpirationDate("2027-12-31")
                .cardActiveStatus("Y")
                .build();

        cardDto = CardDto.builder()
                .cardNum("4111111111111111")
                .cardAcctId(1L)
                .cardCvvCd(123)
                .cardEmbossedName("John Doe")
                .cardExpirationDate("2027-12-31")
                .cardActiveStatus("Y")
                .build();
    }

    @Test
    void listCards() {
        Page<Card> page = new PageImpl<>(List.of(card), PageRequest.of(0, 20), 1);
        when(cardRepository.findAll(PageRequest.of(0, 20))).thenReturn(page);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        PagedResponse<CardDto> result = cardService.listCards(0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getCard_found() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        CardDto result = cardService.getCard("4111111111111111");

        assertThat(result).isEqualTo(cardDto);
    }

    @Test
    void getCard_notFound() {
        when(cardRepository.findById("0000000000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCard("0000000000000000"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Card not found");
    }

    @Test
    void updateCard_success() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toDto(any(Card.class))).thenReturn(cardDto);

        CardDto result = cardService.updateCard("4111111111111111", cardDto);

        assertThat(result).isEqualTo(cardDto);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void updateCard_notFound() {
        when(cardRepository.findById("0000000000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.updateCard("0000000000000000", cardDto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCardsByAccount() {
        when(cardRepository.findByCardAcctId(1L)).thenReturn(List.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        List<CardDto> result = cardService.getCardsByAccount(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCardNum()).isEqualTo("4111111111111111");
    }
}
