package com.carddemo.backend.account.controller;

import com.carddemo.backend.account.service.AccountService;
import com.carddemo.common.dto.AccountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable Long id, @RequestBody AccountDto dto) {
        return ResponseEntity.ok(accountService.update(id, dto));
    }

    @GetMapping
    public ResponseEntity<Page<AccountDto>> listAccounts(Pageable pageable) {
        return ResponseEntity.ok(accountService.findAll(pageable));
    }

    @GetMapping("/export")
    public ResponseEntity<List<AccountDto>> exportAccounts() {
        return ResponseEntity.ok(accountService.findAll());
    }
}
