package com.bank.controller;

import com.bank.dto.TransactionResponse;
import com.bank.entity.Limit;
import com.bank.repository.LimitRepository;
import com.bank.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for client-related operations.
 */
@RestController
@RequestMapping("/api/client")
@Tag(name = "Client API", description = "Endpoints for managing client transactions and limits")
public class ClientController {

    private final TransactionService transactionService;
    private final LimitRepository limitRepository;

    @Autowired
    public ClientController(TransactionService transactionService,
        LimitRepository limitRepository) {
        this.transactionService = transactionService;
        this.limitRepository = limitRepository;
    }

    /**
     * Retrieves a list of transactions that exceeded their limits.
     *
     * @return a list of exceeded transactions
     */
    @GetMapping("/exceeded")
    @Operation(summary = "Get exceeded transactions", description = "Returns a list of transactions that exceeded their respective limits")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved exceeded transactions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<TransactionResponse> getExceededTransactions() {
        return transactionService.getExceededTransactions();
    }

    /**
     * Retrieves a list of all limits.
     *
     * @return a list of all limits
     */
    @GetMapping("/limits")
    @Operation(summary = "Get all limits", description = "Returns a list of all defined limits")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all limits"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<Limit> getAllLimits() {
        return limitRepository.findAll();
    }
}