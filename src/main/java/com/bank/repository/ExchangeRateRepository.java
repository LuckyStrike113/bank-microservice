package com.bank.repository;

import com.bank.entity.ExchangeRate;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(
        String currencyPair, LocalDate date
    );
}