package com.bank.repository;

import com.bank.entity.ExchangeRate;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing {@link ExchangeRate} entities.
 */
@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    /**
     * Finds the latest exchange rate for a currency pair before or on a specific date.
     *
     * @param currencyPair the currency pair
     * @param date         the cutoff date
     * @return an optional containing the latest exchange rate, or empty if none found
     */
    Optional<ExchangeRate> findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(
        String currencyPair, LocalDate date);
}