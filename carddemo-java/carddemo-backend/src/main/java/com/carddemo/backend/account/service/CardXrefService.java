package com.carddemo.backend.account.service;

import com.carddemo.backend.account.entity.CardXrefEntity;
import com.carddemo.backend.account.repository.CardXrefRepository;
import com.carddemo.common.dto.CardXrefDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardXrefService {

    private final CardXrefRepository cardXrefRepository;

    public CardXrefService(CardXrefRepository cardXrefRepository) {
        this.cardXrefRepository = cardXrefRepository;
    }

    public CardXrefDto findByCardNum(String cardNum) {
        CardXrefEntity entity = cardXrefRepository.findByXrefCardNum(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("CardXref", "xrefCardNum", cardNum));
        return toDto(entity);
    }

    public List<CardXrefDto> findByAcctId(Long acctId) {
        List<CardXrefEntity> entities = cardXrefRepository.findByXrefAcctId(acctId);
        return entities.stream().map(this::toDto).toList();
    }

    private CardXrefDto toDto(CardXrefEntity entity) {
        return new CardXrefDto(entity.getXrefCardNum(), entity.getXrefCustId(), entity.getXrefAcctId());
    }
}
