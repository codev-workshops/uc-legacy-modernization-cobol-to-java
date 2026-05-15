package com.carddemo.controller;

import com.carddemo.model.Card;
import com.carddemo.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public List<Card> getAll() {
        return cardService.findAll();
    }

    @GetMapping("/{cardNumber}")
    public ResponseEntity<Card> getByCardNumber(@PathVariable String cardNumber) {
        return cardService.findByCardNumber(cardNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/account/{accountId}")
    public List<Card> getByAccountId(@PathVariable Long accountId) {
        return cardService.findByAccountId(accountId);
    }

    @PostMapping
    public Card create(@RequestBody Card card) {
        return cardService.save(card);
    }

    @PutMapping("/{cardNumber}")
    public ResponseEntity<Card> update(@PathVariable String cardNumber, @RequestBody Card card) {
        return cardService.findByCardNumber(cardNumber)
                .map(existing -> {
                    card.setCardNumber(cardNumber);
                    return ResponseEntity.ok(cardService.save(card));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{cardNumber}")
    public ResponseEntity<Void> delete(@PathVariable String cardNumber) {
        return cardService.findByCardNumber(cardNumber)
                .map(existing -> {
                    cardService.deleteByCardNumber(cardNumber);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
