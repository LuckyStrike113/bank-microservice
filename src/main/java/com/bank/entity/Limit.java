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
 * Entity representing a spending limit for a category.
 */
@Entity
@Table(name = "limits")
@Getter
@Setter
@NoArgsConstructor
public class Limit {

    /**
     * The unique identifier of the limit.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The expense category of the limit.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "expense_category", nullable = false)
    private ExpenseCategory expenseCategory;

    /**
     * The limit amount in USD.
     */
    @Column(name = "limit_sum", nullable = false, precision = 23, scale = 4)
    private BigDecimal limitSum;

    /**
     * The date and time when the limit was set.
     */
    @Column(name = "limit_datetime", nullable = false)
    private OffsetDateTime limitDatetime;
}