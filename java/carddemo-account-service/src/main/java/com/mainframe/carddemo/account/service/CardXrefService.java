package com.mainframe.carddemo.account.service;

import com.mainframe.carddemo.account.entity.CardXref;
import com.mainframe.carddemo.account.repository.CardXrefRepository;
import com.mainframe.carddemo.common.dto.CardXrefDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardXrefService {

    private final CardXrefRepository cardXrefRepository;

    public CardXrefService(CardXrefRepository cardXrefRepository) {
        this.cardXrefRepository = cardXrefRepository;
    }

    public CardXrefDto getByCardNum(String cardNum) {
        CardXref xref = cardXrefRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("Card xref not found: " + cardNum));
        return toDto(xref);
    }

    public List<CardXrefDto> getByAccountId(Long acctId) {
        return cardXrefRepository.findByXrefAcctId(acctId).stream()
                .map(CardXrefService::toDto)
                .collect(Collectors.toList());
    }

    public static CardXrefDto toDto(CardXref xref) {
        CardXrefDto dto = new CardXrefDto();
        dto.setCardNum(xref.getXrefCardNum());
        dto.setCustomerId(xref.getXrefCustId());
        dto.setAccountId(xref.getXrefAcctId());
        return dto;
    }
}
