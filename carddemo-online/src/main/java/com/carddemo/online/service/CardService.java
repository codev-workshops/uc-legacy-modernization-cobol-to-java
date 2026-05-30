package com.carddemo.online.service;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Card;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.CustomerRepository;
import com.carddemo.online.dto.CardDetailResponse;
import com.carddemo.online.dto.CardResponse;
import com.carddemo.online.dto.CardUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public CardService(CardRepository cardRepository,
                       CardXrefRepository cardXrefRepository,
                       AccountRepository accountRepository,
                       CustomerRepository customerRepository) {
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public Page<CardResponse> listCards(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::toResponse);
    }

    public CardDetailResponse getCard(String cardNum) {
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + cardNum));

        CardDetailResponse detail = new CardDetailResponse();
        detail.setCardNum(card.getCardNum());
        detail.setAcctId(card.getAcctId());
        detail.setCvvCd(card.getCvvCd());
        detail.setEmbossedName(card.getEmbossedName());
        detail.setExpirationDate(card.getExpirationDate());
        detail.setActiveStatus(card.getActiveStatus());

        Optional<CardXref> xref = cardXrefRepository.findById(cardNum);
        if (xref.isPresent()) {
            CardXref x = xref.get();
            accountRepository.findById(x.getAcctId()).ifPresent(acct ->
                    detail.setAccount(toAccountSummary(acct)));
            customerRepository.findById(x.getCustId()).ifPresent(cust ->
                    detail.setCustomer(toCustomerSummary(cust)));
        }

        return detail;
    }

    public CardResponse updateCard(String cardNum, CardUpdateRequest request) {
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + cardNum));

        if (request.getAcctId() != null) {
            card.setAcctId(request.getAcctId());
        }
        if (request.getCvvCd() != null) {
            card.setCvvCd(request.getCvvCd());
        }
        if (request.getEmbossedName() != null) {
            card.setEmbossedName(request.getEmbossedName());
        }
        if (request.getExpirationDate() != null) {
            card.setExpirationDate(request.getExpirationDate());
        }
        if (request.getActiveStatus() != null) {
            card.setActiveStatus(request.getActiveStatus());
        }

        cardRepository.save(card);
        return toResponse(card);
    }

    private CardResponse toResponse(Card card) {
        return new CardResponse(
                card.getCardNum(),
                card.getAcctId(),
                card.getCvvCd(),
                card.getEmbossedName(),
                card.getExpirationDate(),
                card.getActiveStatus()
        );
    }

    private CardDetailResponse.AccountSummary toAccountSummary(Account acct) {
        return new CardDetailResponse.AccountSummary(
                acct.getAcctId(),
                acct.getActiveStatus(),
                acct.getCurrBal(),
                acct.getCreditLimit(),
                acct.getExpirationDate()
        );
    }

    private CardDetailResponse.CustomerSummary toCustomerSummary(Customer cust) {
        return new CardDetailResponse.CustomerSummary(
                cust.getCustId(),
                cust.getFirstName(),
                cust.getLastName()
        );
    }

    public static class CardNotFoundException extends RuntimeException {
        public CardNotFoundException(String message) {
            super(message);
        }
    }
}
