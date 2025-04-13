package com.bank.dto;

import com.bank.entity.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for receiving transaction data from clients.
 */
@Getter
@Setter
@NoArgsConstructor
public class TransactionRequest {

    /**
     * The client's bank account number (10 digits).
     */
    @NotBlank(message = "Account from must not be blank")
    @Pattern(regexp = "\\d{10}", message = "Account from must be a 10-digit number")
    private String accountFrom;

    /**
     * The destination account number (10 digits).
     */
    @NotBlank(message = "Account to must not be blank")
    @Pattern(regexp = "\\d{10}", message = "Account to must be a 10-digit number")
    private String accountTo;

    /**
     * The currency code (3 letters, e.g., KZT, USD).
     */
    @NotBlank(message = "Currency shortname must not be blank")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency shortname must be a 3-letter code")
    private String currencyShortname;

    /**
     * The transaction amount (up to 19 integer and 4 fractional digits).
     */
    @NotNull(message = "Sum must not be null")
    @DecimalMin(value = "0.01", message = "Sum must be greater than or equal to 0.01")
    @Digits(integer = 19, fraction = 4, message = "Sum must have up to 19 integer digits and 4 fractional digits")
    private BigDecimal sum;

    /**
     * The expense category of the transaction (PRODUCT or SERVICE).
     */
    @NotNull(message = "Expense category must not be null")
    private ExpenseCategory expenseCategory;

    /**
     * The date and time of the transaction (must be in the past or present).
     */
    @NotNull(message = "Datetime must not be null")
    @PastOrPresent(message = "Datetime must be in the past or present")
    private OffsetDateTime datetime;
}