package com.carddemo.transaction.controller;

import com.carddemo.common.dto.TransactionDto;
import com.carddemo.common.model.PagedResponse;
import com.carddemo.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<PagedResponse<TransactionDto>> listTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transactionService.listTransactions(page, size));
    }

    @GetMapping("/{tranId}")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable String tranId) {
        return ResponseEntity.ok(transactionService.getTransaction(tranId));
    }

    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(
            @Valid @RequestBody TransactionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(dto));
    }
}
