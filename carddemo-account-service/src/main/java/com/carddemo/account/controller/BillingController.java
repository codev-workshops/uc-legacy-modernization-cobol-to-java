package com.carddemo.account.controller;

import com.carddemo.account.dto.BillingDto;
import com.carddemo.account.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Billing", description = "Billing information endpoints (replaces COBIL00C)")
public class BillingController {

    private static final Logger log = LoggerFactory.getLogger(BillingController.class);

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/{id}/billing")
    @Operation(summary = "Get account billing", description = "Retrieves billing information for an account (replaces COBIL00C)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Billing info retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<BillingDto> getAccountBilling(
            @Parameter(description = "Account ID") @PathVariable Long id) {
        log.debug("GET /api/accounts/{}/billing", id);
        return ResponseEntity.ok(billingService.getAccountBilling(id));
    }
}
