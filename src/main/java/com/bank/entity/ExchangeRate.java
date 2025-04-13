package com.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing an exchange rate.
 */
@Entity
@Table(name = "exchange_rates")
@Getter
@Setter
@NoArgsConstructor
public class ExchangeRate {

    /**
     * The unique identifier of the exchange rate.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The currency pair (e.g., KZT/USD).
     */
    @Column(name = "currency_pair", nullable = false, length = 7)
    private String currencyPair;

    /**
     * The exchange rate relative to USD.
     */
    @Column(name = "close_rate", nullable = false, precision = 23, scale = 4)
    private BigDecimal closeRate;

    /**
     * The date of the exchange rate.
     */
    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;
}