package com.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "exchange_rates", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"currency_pair", "rate_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "currency_pair", nullable = false)
    @Pattern(regexp = "[A-Z]{3}/[A-Z]{3}")
    private String currencyPair;

    @Column(name = "close_rate", nullable = false)
    @DecimalMin("0.0001")
    private BigDecimal closeRate;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;
}