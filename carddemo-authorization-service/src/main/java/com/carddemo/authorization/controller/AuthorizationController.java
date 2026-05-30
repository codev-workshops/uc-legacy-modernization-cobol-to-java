package com.carddemo.authorization.controller;

import com.carddemo.authorization.dto.AuthorizationDto;
import com.carddemo.authorization.service.AuthorizationService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/authorizations")
@Tag(name = "Authorizations", description = "Authorization management endpoints (replaces COPAUA0C, COPAUS2C)")
public class AuthorizationController {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationController.class);

    private final AuthorizationService authorizationService;

    public AuthorizationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Authorization summary", description = "Returns authorization summary for a card number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully")
    })
    public ResponseEntity<List<AuthorizationDto>> getAuthorizationSummary(
            @Parameter(description = "Card number") @RequestParam String cardNum) {
        log.debug("GET /api/authorizations/summary?cardNum={}", cardNum);
        return ResponseEntity.ok(authorizationService.getAuthorizationSummary(cardNum));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Authorization detail", description = "Returns authorization detail by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authorization found"),
            @ApiResponse(responseCode = "404", description = "Authorization not found")
    })
    public ResponseEntity<AuthorizationDto> getAuthorizationDetail(
            @Parameter(description = "Authorization ID") @PathVariable Integer id) {
        log.debug("GET /api/authorizations/{}", id);
        return ResponseEntity.ok(authorizationService.getAuthorizationDetail(id));
    }

    @PostMapping("/{id}/mark-fraud")
    @Operation(summary = "Mark as fraud", description = "Marks an authorization as fraudulent and copies to auth_fraud table")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authorization marked as fraud"),
            @ApiResponse(responseCode = "404", description = "Authorization not found")
    })
    public ResponseEntity<AuthorizationDto> markAsFraud(
            @Parameter(description = "Authorization ID") @PathVariable Integer id) {
        log.debug("POST /api/authorizations/{}/mark-fraud", id);
        return ResponseEntity.ok(authorizationService.markAsFraud(id));
    }
}
