package com.mainframe.carddemo.account.controller;

import com.mainframe.carddemo.account.service.CardService;
import com.mainframe.carddemo.common.dto.CardDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<List<CardDto>> getCardsByAccount(@RequestParam("acctId") Long acctId) {
        return ResponseEntity.ok(cardService.getCardsByAccountId(acctId));
    }

    @GetMapping("/{num}")
    public ResponseEntity<CardDto> getCard(@PathVariable("num") String num) {
        return ResponseEntity.ok(cardService.getCardByNum(num));
    }

    @PutMapping("/{num}")
    public ResponseEntity<CardDto> updateCard(@PathVariable("num") String num, @RequestBody CardDto dto) {
        return ResponseEntity.ok(cardService.updateCard(num, dto));
    }
}
