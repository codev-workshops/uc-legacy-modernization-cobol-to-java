package com.carddemo.service;

import com.carddemo.model.Card;
import com.carddemo.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<Card> findAll() {
        return cardRepository.findAll();
    }

    public Optional<Card> findByCardNumber(String cardNumber) {
        return cardRepository.findById(cardNumber);
    }

    public List<Card> findByAccountId(Long accountId) {
        return cardRepository.findByAccountId(accountId);
    }

    public Card save(Card card) {
        return cardRepository.save(card);
    }

    public void deleteByCardNumber(String cardNumber) {
        cardRepository.deleteById(cardNumber);
    }
}
