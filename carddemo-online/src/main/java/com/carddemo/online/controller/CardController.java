package com.carddemo.online.controller;

import com.carddemo.online.dto.CardDetailResponse;
import com.carddemo.online.dto.CardResponse;
import com.carddemo.online.dto.CardUpdateRequest;
import com.carddemo.online.service.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<Page<CardResponse>> listCards(Pageable pageable) {
        return ResponseEntity.ok(cardService.listCards(pageable));
    }

    @GetMapping("/{cardNum}")
    public ResponseEntity<CardDetailResponse> getCard(@PathVariable String cardNum) {
        return ResponseEntity.ok(cardService.getCard(cardNum));
    }

    @PutMapping("/{cardNum}")
    public ResponseEntity<CardResponse> updateCard(
            @PathVariable String cardNum,
            @Valid @RequestBody CardUpdateRequest request) {
        return ResponseEntity.ok(cardService.updateCard(cardNum, request));
    }
}
