package com.bank.service;

import com.bank.client.ExchangeRateClient;
import com.bank.entity.ExchangeRate;
import com.bank.exception.ExchangeRateApiException;
import com.bank.mapper.ExchangeRateMapper;
import com.bank.repository.ExchangeRateRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for retrieving and caching exchange rates relative to USD.
 */
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);
    private static final List<String> POPULAR_CURRENCIES = List.of(
        "KZT", "RUB", "BYN", "CNY", "JPY", "EUR", "GBP");
    private static final String EXCHANGE_ZONE_ID = "America/New_York";
    private static final int EXCHANGE_CLOSE_HOUR = 17; // 17:00 EST/EDT

    private final ExchangeRateClient exchangeRateClient;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateMapper exchangeRateMapper;
    private final Clock clock;

    /**
     * Retrieves the exchange rate for a currency on a given date. Returns the rate relative to USD
     * (e.g., KZT/USD). For current date before 17:00 EST/EDT, uses the previous working day's rate.
     * For weekends or holidays (Dec 25, Jan 1), uses the last working day's rate.
     *
     * @param currency the currency code (e.g., "KZT")
     * @param date     the date for the rate (must not be in the future)
     * @return the exchange rate (currency/USD)
     * @throws IllegalArgumentException if the currency is invalid, date is in the future, or rate
     *                                  is unavailable
     * @throws ExchangeRateApiException if the external API fails
     */
    @Transactional(readOnly = true)
    public BigDecimal getRate(String currency, LocalDate date) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency code must not be empty");
        }
        if (date.isAfter(LocalDate.now(clock))) {
            throw new IllegalArgumentException("Cannot fetch rate for future date: " + date);
        }
        if ("USD".equalsIgnoreCase(currency)) {
            return BigDecimal.ONE;
        }

        String pair = currency + "/USD";
        return exchangeRateRepository
            .findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(pair, date)
            .map(rate -> {
                log.debug("Found rate for {} on {}: {}", pair, date, rate.getCloseRate());
                return rate.getCloseRate();
            })
            .orElseGet(() -> {
                log.info("Rate for {} on {} not found, fetching rates", pair, date);
                fetchRatesForDate(currency, date);
                return exchangeRateRepository
                    .findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(pair, date)
                    .map(ExchangeRate::getCloseRate)
                    .orElseThrow(() -> new IllegalArgumentException(
                        "No rate available for " + pair + " on " + date));
            });
    }

    /**
     * Fetches exchange rates for the specified currency and popular currencies, caching them in the
     * database. Adjusts the fetch date for weekends, holidays, or before 17:00 EST/EDT.
     *
     * @param requestedCurrency the currency that triggered the fetch (e.g., "KZT")
     * @param date              the requested date
     * @throws IllegalArgumentException if the currency is invalid
     * @throws ExchangeRateApiException if the external API fails
     */
    @Transactional
    public void fetchRatesForDate(String requestedCurrency, LocalDate date) {
        if (requestedCurrency == null || requestedCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency code must not be empty");
        }

        LocalDate fetchDate = determineFetchDate(date);
        log.debug("Determined fetchDate: {} for requested date: {}", fetchDate, date);

        // Собираем валюты для запроса
        List<String> currenciesToFetch = new ArrayList<>();
        if (!"USD".equalsIgnoreCase(requestedCurrency) &&
            !POPULAR_CURRENCIES.contains(requestedCurrency)) {
            currenciesToFetch.add(requestedCurrency);
        }
        OffsetDateTime nowInExchangeZone = OffsetDateTime.now(
            clock.withZone(ZoneId.of(EXCHANGE_ZONE_ID)));
        boolean isAfterClose = nowInExchangeZone.getHour() >= EXCHANGE_CLOSE_HOUR;
        if (fetchDate.equals(LocalDate.now(clock)) && isAfterClose) {
            currenciesToFetch.addAll(POPULAR_CURRENCIES);
        } else if (!currenciesToFetch.contains(requestedCurrency)) {
            currenciesToFetch.add(requestedCurrency);
        }

        // Проверяем существующие валюты в БД
        List<String> existingPairs = exchangeRateRepository.findCurrencyPairsByRateDate(fetchDate);
        log.debug("Existing pairs for {}: {}", fetchDate, existingPairs);
        Set<String> existingCurrencies = existingPairs.stream()
            .map(pair -> pair.split("/")[0])
            .collect(Collectors.toSet());
        log.debug("Existing currencies for {}: {}", fetchDate, existingCurrencies);

        currenciesToFetch = currenciesToFetch.stream()
            .filter(currency -> !existingCurrencies.contains(currency))
            .toList();
        log.debug("Currencies to fetch for {}: {}", fetchDate, currenciesToFetch);

        if (currenciesToFetch.isEmpty()) {
            log.info("All required rates for {} already exist", fetchDate);
            return;
        }

        try {
            log.debug("Fetching rates for currencies: {}, date: {}", currenciesToFetch, fetchDate);
            Map<String, BigDecimal> rates = exchangeRateClient.getHistoricalRates(currenciesToFetch,
                fetchDate);
            List<ExchangeRate> ratesToSave = rates.entrySet().stream()
                .map(entry -> exchangeRateMapper.toEntity(
                    entry.getKey() + "/USD", entry.getValue(), fetchDate))
                .toList();

            if (ratesToSave.isEmpty()) {
                log.warn("No valid rates received for date {}", fetchDate);
                throw new ExchangeRateApiException(
                    "No valid rates received for " + fetchDate +
                    ". Please check the currency code for correctness.");
            }

            exchangeRateRepository.saveAll(ratesToSave);
            log.info("Saved {} rates for date {}", ratesToSave.size(), fetchDate);
        } catch (ExchangeRateApiException e) {
            log.error("Failed to fetch rates for {}: {}", fetchDate, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching rates for {}: {}", fetchDate, e.getMessage());
            throw new ExchangeRateApiException(
                "Unexpected error fetching rates for " + fetchDate +
                ". Please check API key or try again later.", e);
        }
    }

    /**
     * Determines the date to fetch rates for, adjusting for exchange close time (17:00 EST/EDT),
     * weekends, and holidays (Dec 25, Jan 1).
     *
     * @param date the requested date
     * @return the date to fetch rates for
     */
    private LocalDate determineFetchDate(LocalDate date) {
        OffsetDateTime now = OffsetDateTime.now(clock.withZone(ZoneId.of(EXCHANGE_ZONE_ID)));
        LocalDate today = now.toLocalDate();
        OffsetDateTime closeTimeToday = today.atTime(EXCHANGE_CLOSE_HOUR, 0)
            .atZone(ZoneId.of(EXCHANGE_ZONE_ID))
            .toOffsetDateTime();

        // Если текущий день и до 17:00 EST/EDT, берём предыдущий рабочий день
        if (date.equals(today) && now.isBefore(closeTimeToday)) {
            LocalDate candidate = today.minusDays(1);
            while (isNonWorkingDay(candidate)) {
                candidate = candidate.minusDays(1);
            }
            log.debug("Date {} is today and before close time {}, using: {}",
                date, closeTimeToday, candidate);
            return candidate;
        }

        // Если день — выходной или праздник, берём предыдущий рабочий день
        if (isNonWorkingDay(date)) {
            LocalDate candidate = date.minusDays(1);
            while (isNonWorkingDay(candidate)) {
                candidate = candidate.minusDays(1);
            }
            log.debug("Date {} is non-working, using: {}", date, candidate);
            return candidate;
        }

        log.debug("Date {} is a working day, using it directly", date);
        return date;
    }

    /**
     * Checks if the date is a non-working day (weekend or holiday: Dec 25, Jan 1).
     *
     * @param date the date to check
     * @return true if the date is a non-working day
     */
    private boolean isNonWorkingDay(LocalDate date) {
        Set<LocalDate> holidays = Set.of(
            LocalDate.of(date.getYear(), 12, 25),
            LocalDate.of(date.getYear(), 1, 1)
        );
        boolean isNonWorking = date.getDayOfWeek().getValue() >= 6 || holidays.contains(date);
        log.debug("Date {} is non-working: {}", date, isNonWorking);
        return isNonWorking;
    }
}