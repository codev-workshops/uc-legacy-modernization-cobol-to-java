package com.carddemo.backend.account.controller;

import com.carddemo.backend.account.service.CardService;
import com.carddemo.common.dto.CardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/v1/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<Page<CardDto>> listCards(
            @RequestParam(required = false) Long acctId,
            Pageable pageable) {
        if (acctId != null) {
            return ResponseEntity.ok(cardService.findByAcctId(acctId, pageable));
        }
        return ResponseEntity.ok(cardService.findAll(pageable));
    }

    @GetMapping("/{num}")
    public ResponseEntity<CardDto> getCard(@PathVariable String num) {
        return ResponseEntity.ok(cardService.findByCardNum(num));
    }

    @PutMapping("/{num}")
    public ResponseEntity<CardDto> updateCard(@PathVariable String num, @RequestBody CardDto dto) {
        return ResponseEntity.ok(cardService.update(num, dto));
    }

    @GetMapping("/export")
    public ResponseEntity<List<CardDto>> exportCards() {
        return ResponseEntity.ok(cardService.findAll());
    }
}
