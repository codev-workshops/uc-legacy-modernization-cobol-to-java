package com.mainframe.carddemo.account.controller;

import com.mainframe.carddemo.account.service.AccountService;
import com.mainframe.carddemo.account.service.CardXrefService;
import com.mainframe.carddemo.common.dto.AccountDto;
import com.mainframe.carddemo.common.dto.BalanceUpdateDto;
import com.mainframe.carddemo.common.dto.CardXrefDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal")
public class InternalController {

    private final CardXrefService cardXrefService;
    private final AccountService accountService;

    public InternalController(CardXrefService cardXrefService, AccountService accountService) {
        this.cardXrefService = cardXrefService;
        this.accountService = accountService;
    }

    @GetMapping("/xref/{cardNum}")
    public ResponseEntity<CardXrefDto> getXrefByCardNum(@PathVariable String cardNum) {
        return ResponseEntity.ok(cardXrefService.getByCardNum(cardNum));
    }

    @GetMapping("/xref/byAccount/{acctId}")
    public ResponseEntity<List<CardXrefDto>> getXrefByAccountId(@PathVariable Long acctId) {
        return ResponseEntity.ok(cardXrefService.getByAccountId(acctId));
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<AccountDto> getAccountInternal(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @PutMapping("/accounts/{id}/balance")
    public ResponseEntity<AccountDto> updateBalance(@PathVariable Long id, @RequestBody BalanceUpdateDto dto) {
        return ResponseEntity.ok(accountService.updateBalance(id, dto));
    }
}
