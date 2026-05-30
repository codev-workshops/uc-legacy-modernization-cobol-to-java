package com.carddemo.account.controller;

import com.carddemo.account.service.CardXrefService;
import com.carddemo.common.dto.CardXrefDto;
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
@RequestMapping("/api/card-xref")
@Tag(name = "Card Cross-Reference", description = "Card-to-customer-to-account cross-reference lookup (Feign endpoint)")
public class CardXrefController {

    private static final Logger log = LoggerFactory.getLogger(CardXrefController.class);

    private final CardXrefService cardXrefService;

    public CardXrefController(CardXrefService cardXrefService) {
        this.cardXrefService = cardXrefService;
    }

    @GetMapping("/{cardNum}")
    @Operation(summary = "Get card xref", description = "Retrieves the cross-reference entry for a card number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card xref found"),
            @ApiResponse(responseCode = "404", description = "Card xref not found")
    })
    public ResponseEntity<CardXrefDto> getCardXref(
            @Parameter(description = "Card number") @PathVariable String cardNum) {
        log.debug("GET /api/card-xref/{}", cardNum);
        return ResponseEntity.ok(cardXrefService.getCardXref(cardNum));
    }
}
