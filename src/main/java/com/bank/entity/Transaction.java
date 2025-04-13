package com.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a bank transaction.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    /**
     * The unique identifier of the transaction.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The source account number.
     */
    @Column(name = "account_from", nullable = false, length = 10)
    private String accountFrom;

    /**
     * The destination account number.
     */
    @Column(name = "account_to", nullable = false, length = 10)
    private String accountTo;

    /**
     * The currency code of the transaction.
     */
    @Column(name = "currency_shortname", nullable = false, length = 3)
    private String currencyShortname;

    /**
     * The transaction amount.
     */
    @Column(nullable = false, precision = 23, scale = 4)
    private BigDecimal sum;

    /**
     * The expense category of the transaction.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "expense_category", nullable = false)
    private ExpenseCategory expenseCategory;

    /**
     * The date and time of the transaction.
     */
    @Column(nullable = false)
    private OffsetDateTime datetime;

    /**
     * Indicates whether the transaction exceeded the spending limit.
     */
    @Column(name = "limit_exceeded", nullable = false)
    private boolean limitExceeded;
}