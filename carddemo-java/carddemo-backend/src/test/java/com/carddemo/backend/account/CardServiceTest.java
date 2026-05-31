package com.carddemo.backend.account;

import com.carddemo.backend.account.entity.CardEntity;
import com.carddemo.backend.account.repository.AccountRepository;
import com.carddemo.backend.account.repository.CardRepository;
import com.carddemo.backend.account.service.CardService;
import com.carddemo.common.dto.CardDto;
import com.carddemo.common.exception.BusinessRuleViolationException;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CardService cardService;

    private CardEntity sampleCard;

    @BeforeEach
    void setUp() {
        sampleCard = new CardEntity();
        sampleCard.setCardNum("0500024453765740");
        sampleCard.setCardAcctId(50L);
        sampleCard.setCvvCd(747);
        sampleCard.setEmbossedName("Aniya Von");
        sampleCard.setExpirationDate("2023-03-09");
        sampleCard.setActiveStatus("Y");
    }

    @Test
    void findByCardNum_returnsDto() {
        when(cardRepository.findById("0500024453765740")).thenReturn(Optional.of(sampleCard));

        CardDto result = cardService.findByCardNum("0500024453765740");

        assertEquals("0500024453765740", result.getCardNumber());
        assertEquals(50L, result.getAccountId());
        assertEquals("Y", result.getCardStatus());
        assertEquals(LocalDate.of(2023, 3, 9), result.getExpirationDate());
        assertEquals(747, result.getCvvCode());
        assertEquals("Aniya Von", result.getEmbossedName());
    }

    @Test
    void findByCardNum_notFound() {
        when(cardRepository.findById("9999999999999999")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cardService.findByCardNum("9999999999999999"));
    }

    @Test
    void findAll_paginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardEntity> page = new PageImpl<>(List.of(sampleCard), pageable, 1);
        when(cardRepository.findAll(pageable)).thenReturn(page);

        Page<CardDto> result = cardService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("0500024453765740", result.getContent().get(0).getCardNumber());
    }

    @Test
    void findByAcctId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardEntity> page = new PageImpl<>(List.of(sampleCard), pageable, 1);
        when(cardRepository.findByCardAcctId(50L, pageable)).thenReturn(page);

        Page<CardDto> result = cardService.findByAcctId(50L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(50L, result.getContent().get(0).getAccountId());
    }

    @Test
    void findAll_list() {
        when(cardRepository.findAll()).thenReturn(List.of(sampleCard));

        List<CardDto> result = cardService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void update_success() {
        when(cardRepository.findById("0500024453765740")).thenReturn(Optional.of(sampleCard));
        when(accountRepository.existsById(50L)).thenReturn(true);
        when(cardRepository.save(any())).thenReturn(sampleCard);

        CardDto dto = new CardDto();
        dto.setAccountId(50L);
        dto.setCardStatus("N");
        dto.setExpirationDate(LocalDate.of(2026, 1, 1));
        dto.setEmbossedName("Updated Name");
        dto.setCvvCode(999);

        CardDto result = cardService.update("0500024453765740", dto);

        assertNotNull(result);
        verify(cardRepository).save(any());
    }

    @Test
    void update_notFound() {
        when(cardRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        CardDto dto = new CardDto();
        assertThrows(ResourceNotFoundException.class, () -> cardService.update("9999999999999999", dto));
    }

    @Test
    void update_accountNotFound() {
        when(cardRepository.findById("0500024453765740")).thenReturn(Optional.of(sampleCard));
        when(accountRepository.existsById(999L)).thenReturn(false);

        CardDto dto = new CardDto();
        dto.setAccountId(999L);

        assertThrows(BusinessRuleViolationException.class,
                () -> cardService.update("0500024453765740", dto));
    }
}
