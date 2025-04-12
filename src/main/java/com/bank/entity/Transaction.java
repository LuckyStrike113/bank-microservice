package com.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a bank transaction entity stored in the database.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_from", nullable = false)
    @Size(min = 10, max = 10)
    private String accountFrom;

    @Column(name = "account_to", nullable = false)
    @Size(min = 10, max = 10)
    private String accountTo;

    @Column(name = "currency_shortname", nullable = false)
    @Pattern(regexp = "[A-Z]{3}")
    private String currencyShortname;

    @Column(name = "sum", nullable = false)
    private BigDecimal sum;

    @Column(name = "expense_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExpenseCategory expenseCategory;

    @Column(name = "datetime", nullable = false)
    private OffsetDateTime datetime;

    @Column(name = "limit_exceeded", nullable = false)
    private boolean limitExceeded;
}