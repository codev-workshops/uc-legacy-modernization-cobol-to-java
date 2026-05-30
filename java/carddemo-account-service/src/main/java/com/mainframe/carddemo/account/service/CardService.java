package com.mainframe.carddemo.account.service;

import com.mainframe.carddemo.account.entity.Card;
import com.mainframe.carddemo.account.repository.CardRepository;
import com.mainframe.carddemo.common.dto.CardDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<CardDto> getCardsByAccountId(Long acctId) {
        return cardRepository.findByCardAcctId(acctId).stream()
                .map(CardService::toDto)
                .collect(Collectors.toList());
    }

    public CardDto getCardByNum(String cardNum) {
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardNum));
        return toDto(card);
    }

    @Transactional
    public CardDto updateCard(String cardNum, CardDto dto) {
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardNum));
        if (dto.getEmbossedName() != null) {
            card.setCardEmbossedName(dto.getEmbossedName());
        }
        if (dto.getExpirationDate() != null) {
            card.setCardExpirationDate(dto.getExpirationDate());
        }
        if (dto.getActiveStatus() != null) {
            card.setCardActiveStatus(dto.getActiveStatus());
        }
        Card saved = cardRepository.save(card);
        return toDto(saved);
    }

    public static CardDto toDto(Card card) {
        CardDto dto = new CardDto();
        dto.setCardNum(card.getCardNum());
        dto.setAccountId(card.getCardAcctId());
        dto.setCvvCode(card.getCardCvvCd());
        dto.setEmbossedName(card.getCardEmbossedName());
        dto.setExpirationDate(card.getCardExpirationDate());
        dto.setActiveStatus(card.getCardActiveStatus());
        return dto;
    }
}
