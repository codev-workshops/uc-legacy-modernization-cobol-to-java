package com.mainframe.carddemo.transaction.controller;

import com.mainframe.carddemo.transaction.entity.Transaction;
import com.mainframe.carddemo.transaction.service.BillPaymentRequest;
import com.mainframe.carddemo.transaction.service.TransactionCreateRequest;
import com.mainframe.carddemo.transaction.service.TransactionService;
import org.springframework.data.domain.Page;
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
@RequestMapping("/api")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<Transaction>> listTransactions(
            @RequestParam Long acctId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccount(acctId, page, size));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable String id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @PostMapping("/transactions")
    public ResponseEntity<Transaction> createTransaction(@RequestBody TransactionCreateRequest request) {
        Transaction created = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/billing/pay")
    public ResponseEntity<Transaction> billPayment(@RequestBody BillPaymentRequest request) {
        Transaction payment = transactionService.processBillPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
}
