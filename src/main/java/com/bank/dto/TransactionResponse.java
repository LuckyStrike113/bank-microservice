package com.bank.dto;

import com.bank.entity.ExpenseCategory;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Data;

/**
 * DTO for returning transaction details to clients, including limit information.
 */
@Data
public class TransactionResponse {

    /**
     * The client's bank account number.
     */
    private String accountFrom;

    /**
     * The destination account number.
     */
    private String accountTo;

    /**
     * The currency code of the transaction.
     */
    private String currencyShortname;

    /**
     * The transaction amount.
     */
    private BigDecimal sum;

    /**
     * The expense category of the transaction.
     */
    private ExpenseCategory expenseCategory;

    /**
     * The date and time of the transaction.
     */
    private OffsetDateTime datetime;

    /**
     * Flag indicating whether the transaction exceeded its limit.
     */
    private boolean limitExceeded;

    /**
     * The sum of the limit applied to the transaction.
     */
    private BigDecimal limitSum;

    /**
     * The date and time when the limit was set.
     */
    private OffsetDateTime limitDatetime;

    /**
     * The currency code of the limit (always USD).
     */
    private String limitCurrencyShortname;
}