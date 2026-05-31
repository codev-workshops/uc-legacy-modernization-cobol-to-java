package com.carddemo.backend.account.controller;

import com.carddemo.backend.account.service.CardXrefService;
import com.carddemo.common.dto.CardXrefDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/xref")
public class CardXrefController {

    private final CardXrefService cardXrefService;

    public CardXrefController(CardXrefService cardXrefService) {
        this.cardXrefService = cardXrefService;
    }

    @GetMapping("/card/{cardNum}")
    public ResponseEntity<CardXrefDto> getByCardNum(@PathVariable String cardNum) {
        return ResponseEntity.ok(cardXrefService.findByCardNum(cardNum));
    }

    @GetMapping("/account/{acctId}")
    public ResponseEntity<List<CardXrefDto>> getByAcctId(@PathVariable Long acctId) {
        return ResponseEntity.ok(cardXrefService.findByAcctId(acctId));
    }
}
