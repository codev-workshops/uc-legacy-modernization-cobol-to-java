package com.carddemo.transaction.controller;

import com.carddemo.common.dto.TransactionTypeDto;
import com.carddemo.transaction.service.TransactionTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transaction-types")
@RequiredArgsConstructor
public class TransactionTypeController {

    private final TransactionTypeService transactionTypeService;

    @GetMapping
    public ResponseEntity<List<TransactionTypeDto>> listTransactionTypes() {
        return ResponseEntity.ok(transactionTypeService.listTransactionTypes());
    }

    @GetMapping("/{typeCode}")
    public ResponseEntity<TransactionTypeDto> getTransactionType(@PathVariable String typeCode) {
        return ResponseEntity.ok(transactionTypeService.getTransactionType(typeCode));
    }

    @PostMapping
    public ResponseEntity<TransactionTypeDto> createTransactionType(
            @Valid @RequestBody TransactionTypeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionTypeService.createTransactionType(dto));
    }

    @PutMapping("/{typeCode}")
    public ResponseEntity<TransactionTypeDto> updateTransactionType(
            @PathVariable String typeCode,
            @Valid @RequestBody TransactionTypeDto dto) {
        return ResponseEntity.ok(transactionTypeService.updateTransactionType(typeCode, dto));
    }

    @DeleteMapping("/{typeCode}")
    public ResponseEntity<Void> deleteTransactionType(@PathVariable String typeCode) {
        transactionTypeService.deleteTransactionType(typeCode);
        return ResponseEntity.noContent().build();
    }
}
