package com.carddemo.account.service;

import com.carddemo.account.mapper.CardXrefMapper;
import com.carddemo.account.repository.CardXrefRepository;
import com.carddemo.common.dto.CardXrefDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CardXrefService {

    private static final Logger log = LoggerFactory.getLogger(CardXrefService.class);

    private final CardXrefRepository cardXrefRepository;
    private final CardXrefMapper cardXrefMapper;

    public CardXrefService(CardXrefRepository cardXrefRepository, CardXrefMapper cardXrefMapper) {
        this.cardXrefRepository = cardXrefRepository;
        this.cardXrefMapper = cardXrefMapper;
    }

    public CardXrefDto getCardXref(String cardNum) {
        log.debug("Fetching card xref for card number: {}", cardNum);
        return cardXrefRepository.findById(cardNum)
                .map(cardXrefMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Card xref not found for card number: " + cardNum));
    }
}
