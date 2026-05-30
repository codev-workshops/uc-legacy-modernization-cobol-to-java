package com.carddemo.account.service;

import com.carddemo.account.entity.Card;
import com.carddemo.account.mapper.CardMapper;
import com.carddemo.account.repository.CardRepository;
import com.carddemo.common.dto.CardDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.model.PagedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CardService {

    private static final Logger log = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    public CardService(CardRepository cardRepository, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
    }

    public PagedResponse<CardDto> listCards(int page, int size) {
        log.debug("Listing cards - page: {}, size: {}", page, size);
        Page<Card> cardPage = cardRepository.findAll(PageRequest.of(page, size));
        return PagedResponse.<CardDto>builder()
                .content(cardPage.getContent().stream().map(cardMapper::toDto).toList())
                .page(cardPage.getNumber())
                .size(cardPage.getSize())
                .totalElements(cardPage.getTotalElements())
                .totalPages(cardPage.getTotalPages())
                .build();
    }

    public CardDto getCard(String cardNum) {
        log.debug("Fetching card with number: {}", cardNum);
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with number: " + cardNum));
        return cardMapper.toDto(card);
    }

    @Transactional
    public CardDto updateCard(String cardNum, CardDto dto) {
        log.debug("Updating card with number: {}", cardNum);
        Card existing = cardRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with number: " + cardNum));

        existing.setCardAcctId(dto.getCardAcctId());
        existing.setCardCvvCd(dto.getCardCvvCd());
        existing.setCardEmbossedName(dto.getCardEmbossedName());
        existing.setCardExpirationDate(dto.getCardExpirationDate());
        existing.setCardActiveStatus(dto.getCardActiveStatus());
        existing.setUpdatedAt(LocalDateTime.now());

        Card saved = cardRepository.save(existing);
        log.info("Card {} updated successfully", cardNum);
        return cardMapper.toDto(saved);
    }

    public List<CardDto> getCardsByAccount(Long acctId) {
        log.debug("Fetching cards for account ID: {}", acctId);
        return cardRepository.findByCardAcctId(acctId).stream()
                .map(cardMapper::toDto)
                .toList();
    }
}
