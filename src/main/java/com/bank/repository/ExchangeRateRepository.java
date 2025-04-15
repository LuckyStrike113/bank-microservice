package com.bank.repository;

import com.bank.entity.ExchangeRate;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for managing ExchangeRate entities.
 */
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    /**
     * Finds the most recent exchange rate for a currency pair on or before the given date.
     *
     * @param currencyPair the currency pair (e.g., "KZT/USD")
     * @param date         the date
     * @return the exchange rate, if found
     */
    Optional<ExchangeRate> findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(
        String currencyPair, LocalDate date);

    /**
     * Finds all currency pairs for a specific rate date.
     *
     * @param date the rate date
     * @return list of currency pairs
     */
    @Query("SELECT DISTINCT e.currencyPair FROM ExchangeRate e WHERE e.rateDate = :date")
    List<String> findCurrencyPairsByRateDate(LocalDate date);
}