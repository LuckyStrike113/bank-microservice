package com.bank.repository;

import com.bank.entity.ExchangeRate;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    @Query("SELECT er FROM ExchangeRate er WHERE er.currencyPair = :pair " +
           "AND er.date = (SELECT MAX(er2.date) FROM ExchangeRate er2 WHERE er2.currencyPair = :pair AND er2.date <= :date)")
    Optional<ExchangeRate> findLatestByPairAndDate(String pair, LocalDate date);
}