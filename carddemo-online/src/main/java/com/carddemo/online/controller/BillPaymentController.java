package com.carddemo.online.controller;

import com.carddemo.online.dto.BillPaymentRequest;
import com.carddemo.online.dto.BillPaymentResponse;
import com.carddemo.online.service.BillPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bills")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;

    public BillPaymentController(BillPaymentService billPaymentService) {
        this.billPaymentService = billPaymentService;
    }

    @PostMapping("/pay")
    public ResponseEntity<BillPaymentResponse> payBill(
            @Valid @RequestBody BillPaymentRequest request) {
        BillPaymentResponse response = billPaymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }
}
