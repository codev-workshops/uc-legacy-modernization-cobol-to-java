package com.carddemo.account.controller;

import com.carddemo.account.service.AccountService;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.model.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Account management endpoints (replaces COACTVWC, COACTUPC)")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    @Operation(summary = "List accounts", description = "Returns a paginated list of all accounts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully")
    })
    public ResponseEntity<PagedResponse<AccountDto>> listAccounts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.debug("GET /api/accounts - page: {}, size: {}", page, size);
        return ResponseEntity.ok(accountService.listAccounts(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID", description = "Retrieves a single account by its ID (replaces COACTVWC)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountDto> getAccount(
            @Parameter(description = "Account ID") @PathVariable Long id) {
        log.debug("GET /api/accounts/{}", id);
        return ResponseEntity.ok(accountService.getAccount(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update account", description = "Updates an existing account (replaces COACTUPC)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountDto> updateAccount(
            @Parameter(description = "Account ID") @PathVariable Long id,
            @Valid @RequestBody AccountDto dto) {
        log.debug("PUT /api/accounts/{}", id);
        return ResponseEntity.ok(accountService.updateAccount(id, dto));
    }
}
