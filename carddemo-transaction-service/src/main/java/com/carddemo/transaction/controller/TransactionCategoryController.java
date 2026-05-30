package com.carddemo.transaction.controller;

import com.carddemo.common.dto.TransactionCategoryDto;
import com.carddemo.transaction.service.TransactionCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transaction-categories")
@RequiredArgsConstructor
public class TransactionCategoryController {

    private final TransactionCategoryService transactionCategoryService;

    @GetMapping
    public ResponseEntity<List<TransactionCategoryDto>> listTransactionCategories() {
        return ResponseEntity.ok(transactionCategoryService.listTransactionCategories());
    }

    @GetMapping("/{typeCd}/{catCd}")
    public ResponseEntity<TransactionCategoryDto> getTransactionCategory(
            @PathVariable String typeCd,
            @PathVariable Integer catCd) {
        return ResponseEntity.ok(
                transactionCategoryService.getTransactionCategory(typeCd, catCd));
    }
}
