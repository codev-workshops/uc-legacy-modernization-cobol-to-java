package com.carddemo.account.controller;

import com.carddemo.account.service.CardService;
import com.carddemo.common.dto.CardDto;
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

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@Tag(name = "Cards", description = "Card management endpoints (replaces COCRDLIC, COCRDSLC, COCRDUPC)")
public class CardController {

    private static final Logger log = LoggerFactory.getLogger(CardController.class);

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    @Operation(summary = "List cards", description = "Returns a paginated list of all cards (replaces COCRDLIC)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cards retrieved successfully")
    })
    public ResponseEntity<PagedResponse<CardDto>> listCards(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.debug("GET /api/cards - page: {}, size: {}", page, size);
        return ResponseEntity.ok(cardService.listCards(page, size));
    }

    @GetMapping("/{cardNum}")
    @Operation(summary = "Get card by number", description = "Retrieves a single card by its card number (replaces COCRDSLC)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card found"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<CardDto> getCard(
            @Parameter(description = "Card number") @PathVariable String cardNum) {
        log.debug("GET /api/cards/{}", cardNum);
        return ResponseEntity.ok(cardService.getCard(cardNum));
    }

    @PutMapping("/{cardNum}")
    @Operation(summary = "Update card", description = "Updates an existing card (replaces COCRDUPC)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<CardDto> updateCard(
            @Parameter(description = "Card number") @PathVariable String cardNum,
            @Valid @RequestBody CardDto dto) {
        log.debug("PUT /api/cards/{}", cardNum);
        return ResponseEntity.ok(cardService.updateCard(cardNum, dto));
    }

    @GetMapping("/by-account/{acctId}")
    @Operation(summary = "List cards by account", description = "Returns all cards associated with an account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cards retrieved successfully")
    })
    public ResponseEntity<List<CardDto>> getCardsByAccount(
            @Parameter(description = "Account ID") @PathVariable Long acctId) {
        log.debug("GET /api/cards/by-account/{}", acctId);
        return ResponseEntity.ok(cardService.getCardsByAccount(acctId));
    }
}
