package com.bank.controller;

import com.bank.dto.TransactionRequest;
import com.bank.dto.TransactionResponse;
import com.bank.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing bank transactions.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction API", description = "API for processing bank transactions")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Creates a new transaction by processing the provided request.
     *
     * @param request the transaction request containing account details, sum, currency, category,
     *                and datetime
     * @return a {@link ResponseEntity} containing the saved transaction details as a
     * {@link TransactionResponse}
     */
    @Operation(summary = "Create a new transaction",
        description =
            "Processes a transaction, converts its value to USD, checks limit exceedance, and saves it. "
            +
            "The datetime must be in the past or present (e.g., '2025-04-13T13:00:00+06:00').")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction created successfully",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
            content = @Content(schema = @Schema(implementation = java.util.Map.class)))
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
        @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.processTransaction(request);
        return ResponseEntity.ok(response);
    }
}