package com.carddemo.backend.account.service;

import com.carddemo.backend.account.entity.CardEntity;
import com.carddemo.backend.account.repository.AccountRepository;
import com.carddemo.backend.account.repository.CardRepository;
import com.carddemo.common.dto.CardDto;
import com.carddemo.common.exception.BusinessRuleViolationException;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;

    public CardService(CardRepository cardRepository, AccountRepository accountRepository) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
    }

    public CardDto findByCardNum(String cardNum) {
        CardEntity entity = cardRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNum", cardNum));
        return toDto(entity);
    }

    public Page<CardDto> findAll(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::toDto);
    }

    public Page<CardDto> findByAcctId(Long acctId, Pageable pageable) {
        return cardRepository.findByCardAcctId(acctId, pageable).map(this::toDto);
    }

    public List<CardDto> findAll() {
        return cardRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public CardDto update(String cardNum, CardDto dto) {
        CardEntity entity = cardRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNum", cardNum));

        if (dto.getAccountId() != null) {
            if (!accountRepository.existsById(dto.getAccountId())) {
                throw new BusinessRuleViolationException("ACCOUNT_NOT_FOUND",
                        "Account does not exist: " + dto.getAccountId());
            }
            entity.setCardAcctId(dto.getAccountId());
        }
        if (dto.getCardStatus() != null) {
            entity.setActiveStatus(dto.getCardStatus());
        }
        if (dto.getExpirationDate() != null) {
            entity.setExpirationDate(dto.getExpirationDate().toString());
        }
        if (dto.getEmbossedName() != null) {
            entity.setEmbossedName(dto.getEmbossedName());
        }
        if (dto.getCvvCode() != null) {
            entity.setCvvCd(dto.getCvvCode());
        }

        CardEntity saved = cardRepository.save(entity);
        return toDto(saved);
    }

    private CardDto toDto(CardEntity entity) {
        CardDto dto = new CardDto();
        dto.setCardNumber(entity.getCardNum());
        dto.setAccountId(entity.getCardAcctId());
        dto.setCardStatus(entity.getActiveStatus());
        if (entity.getExpirationDate() != null && !entity.getExpirationDate().isBlank()) {
            try {
                dto.setExpirationDate(java.time.LocalDate.parse(entity.getExpirationDate()));
            } catch (Exception e) {
                // keep null if unparseable
            }
        }
        dto.setCvvCode(entity.getCvvCd());
        dto.setEmbossedName(entity.getEmbossedName());
        return dto;
    }
}
