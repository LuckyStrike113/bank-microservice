package com.bank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.bank.client.ExchangeRateClient;
import com.bank.entity.ExchangeRate;
import com.bank.exception.ExchangeRateApiException;
import com.bank.mapper.ExchangeRateMapper;
import com.bank.repository.ExchangeRateRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    private static final List<String> POPULAR_CURRENCIES = List.of(
        "KZT", "RUB", "BYN", "CNY", "JPY", "EUR", "GBP");

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @Mock
    private ExchangeRateMapper exchangeRateMapper;

    @Mock
    private Clock clock;

    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        // Проверяем, что clock не null
        assertNotNull(clock, "Clock mock should not be null");
        // Явно создаём ExchangeRateService
        exchangeRateService = new ExchangeRateService(
            exchangeRateClient,
            exchangeRateRepository,
            exchangeRateMapper,
            clock
        );
        // Дебаг: проверяем, что clock внедрён
        assertNotNull(exchangeRateService, "ExchangeRateService should not be null");
    }

    @Test
    void getRate_usd_returnsOne() {
        LocalDate date = LocalDate.of(2025, 4, 15);
        // Мокаем clock для LocalDate.now(clock)
        when(clock.getZone()).thenReturn(ZoneId.of("America/New_York"));
        when(clock.instant()).thenReturn(
            ZonedDateTime.of(2025, 4, 15, 18, 0, 0, 0, ZoneId.of("America/New_York")).toInstant());

        BigDecimal result = exchangeRateService.getRate("USD", date);
        assertEquals(BigDecimal.ONE, result);
        verifyNoInteractions(exchangeRateRepository, exchangeRateClient, exchangeRateMapper);
    }

    @Test
    void getRate_emptyCurrency_throwsIllegalArgumentException() {
        LocalDate date = LocalDate.of(2025, 4, 15);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> exchangeRateService.getRate("", date));
        assertEquals("Currency code must not be empty", exception.getMessage());
        verifyNoInteractions(exchangeRateRepository, exchangeRateClient, exchangeRateMapper, clock);
    }

    @Test
    void getRate_rateInDb_returnsRate() {
        LocalDate date = LocalDate.of(2025, 4, 15);
        String currency = "KZT";
        String pair = "KZT/USD";
        ExchangeRate rate = createRate(pair, "0.0021", date);

        // Мокаем clock для LocalDate.now(clock)
        when(clock.getZone()).thenReturn(ZoneId.of("America/New_York"));
        when(clock.instant()).thenReturn(
            ZonedDateTime.of(2025, 4, 15, 18, 0, 0, 0, ZoneId.of("America/New_York")).toInstant());

        when(
            exchangeRateRepository.findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(
                pair, date))
            .thenReturn(Optional.of(rate));

        BigDecimal result = exchangeRateService.getRate(currency, date);

        assertEquals(new BigDecimal("0.0021"), result);
        verify(
            exchangeRateRepository).findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(
            pair, date);
        verifyNoInteractions(exchangeRateClient, exchangeRateMapper);
    }

    @Test
    void getRate_rateNotInDb_fetchesAndSavesPopularCurrencies() {
        LocalDate date = LocalDate.of(2025, 4, 15);
        String currency = "KZT";
        String pair = "KZT/USD";

        // Мокаем clock
        when(clock.getZone()).thenReturn(ZoneId.of("America/New_York"));
        when(clock.instant()).thenReturn(
            ZonedDateTime.of(2025, 4, 15, 18, 0, 0, 0, ZoneId.of("America/New_York")).toInstant());
        when(clock.withZone(ZoneId.of("America/New_York"))).thenReturn(
            Clock.fixed(
                ZonedDateTime.of(2025, 4, 15, 18, 0, 0, 0, ZoneId.of("America/New_York"))
                    .toInstant(),
                ZoneId.of("America/New_York")));

        when(
            exchangeRateRepository.findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(
                pair, date))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(createRate(pair, "0.0021", date)));
        when(exchangeRateRepository.findCurrencyPairsByRateDate(date))
            .thenReturn(Collections.emptyList());

        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("KZT", new BigDecimal("0.0021"));
        rates.put("RUB", new BigDecimal("0.0130"));
        rates.put("BYN", new BigDecimal("0.3000"));
        rates.put("CNY", new BigDecimal("0.1400"));
        rates.put("JPY", new BigDecimal("0.0066"));
        rates.put("EUR", new BigDecimal("1.0600"));
        rates.put("GBP", new BigDecimal("1.2400"));
        when(exchangeRateClient.getHistoricalRates(POPULAR_CURRENCIES, date))
            .thenReturn(rates);

        ExchangeRate kztRate = createRate("KZT/USD", "0.0021", date);
        when(exchangeRateMapper.toEntity(eq("KZT/USD"), eq(new BigDecimal("0.0021")), eq(date)))
            .thenReturn(kztRate);
        for (String curr : POPULAR_CURRENCIES) {
            if (!curr.equals("KZT")) {
                ExchangeRate rate = createRate(curr + "/USD", rates.get(curr).toString(), date);
                when(exchangeRateMapper.toEntity(eq(curr + "/USD"), eq(rates.get(curr)), eq(date)))
                    .thenReturn(rate);
            }
        }
        when(exchangeRateRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        BigDecimal result = exchangeRateService.getRate(currency, date);

        assertEquals(new BigDecimal("0.0021"), result);
        verify(exchangeRateClient).getHistoricalRates(POPULAR_CURRENCIES, date);
        verify(exchangeRateRepository).saveAll(anyList());
        verify(exchangeRateMapper, times(POPULAR_CURRENCIES.size()))
            .toEntity(anyString(), any(BigDecimal.class), eq(date));
        verify(exchangeRateRepository, times(2))
            .findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(pair, date);
    }

    @Test
    void getRate_beforeCloseTime_fetchesPreviousWorkingDay() {
        LocalDate date = LocalDate.of(2025, 4, 15);
        LocalDate fetchDate = LocalDate.of(2025, 4, 14);
        String currency = "KZT";
        String pair = "KZT/USD";

        // Мокаем clock для 16:00 EDT
        when(clock.getZone()).thenReturn(ZoneId.of("America/New_York"));
        when(clock.instant()).thenReturn(
            ZonedDateTime.of(2025, 4, 15, 16, 0, 0, 0, ZoneId.of("America/New_York")).toInstant());
        when(clock.withZone(ZoneId.of("America/New_York"))).thenReturn(
            Clock.fixed(
                ZonedDateTime.of(2025, 4, 15, 16, 0, 0, 0, ZoneId.of("America/New_York"))
                    .toInstant(),
                ZoneId.of("America/New_York")));

        when(
            exchangeRateRepository.findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(
                pair, date))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(createRate(pair, "0.0021", fetchDate)));
        when(exchangeRateRepository.findCurrencyPairsByRateDate(fetchDate))
            .thenReturn(Collections.emptyList());

        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("KZT", new BigDecimal("0.0021"));
        when(exchangeRateClient.getHistoricalRates(List.of("KZT"), fetchDate))
            .thenReturn(rates);

        ExchangeRate rate = createRate(pair, "0.0021", fetchDate);
        when(exchangeRateMapper.toEntity(pair, new BigDecimal("0.0021"), fetchDate))
            .thenReturn(rate);
        when(exchangeRateRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        BigDecimal result = exchangeRateService.getRate(currency, date);

        assertEquals(new BigDecimal("0.0021"), result);
        verify(exchangeRateClient).getHistoricalRates(List.of("KZT"), fetchDate);
        verify(exchangeRateRepository).saveAll(anyList());
        verify(exchangeRateMapper).toEntity(pair, new BigDecimal("0.0021"), fetchDate);
        verify(exchangeRateRepository, times(2))
            .findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(pair, date);
    }

    @Test
    void getRate_holiday_fetchesLastWorkingDay() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        LocalDate fetchDate = LocalDate.of(2024, 12, 31);
        String currency = "KZT";
        String pair = "KZT/USD";

        // Мокаем clock
        when(clock.getZone()).thenReturn(ZoneId.of("America/New_York"));
        when(clock.instant()).thenReturn(
            ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("America/New_York")).toInstant());
        when(clock.withZone(ZoneId.of("America/New_York"))).thenReturn(
            Clock.fixed(
                ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("America/New_York"))
                    .toInstant(),
                ZoneId.of("America/New_York")));

        when(
            exchangeRateRepository.findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(
                pair, date))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(createRate(pair, "0.0021", fetchDate)));
        when(exchangeRateRepository.findCurrencyPairsByRateDate(fetchDate))
            .thenReturn(Collections.emptyList());

        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("KZT", new BigDecimal("0.0021"));
        when(exchangeRateClient.getHistoricalRates(List.of("KZT"), fetchDate))
            .thenReturn(rates);

        ExchangeRate rate = createRate(pair, "0.0021", fetchDate);
        when(exchangeRateMapper.toEntity(pair, new BigDecimal("0.0021"), fetchDate))
            .thenReturn(rate);
        when(exchangeRateRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        BigDecimal result = exchangeRateService.getRate(currency, date);

        assertEquals(new BigDecimal("0.0021"), result);
        verify(exchangeRateClient).getHistoricalRates(List.of("KZT"), fetchDate);
        verify(exchangeRateRepository).saveAll(anyList());
        verify(exchangeRateMapper).toEntity(pair, new BigDecimal("0.0021"), fetchDate);
        verify(exchangeRateRepository, times(2))
            .findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(pair, date);
    }

    @Test
    void getRate_weekend_fetchesLastWorkingDay() {
        LocalDate date = LocalDate.of(2025, 4, 13);
        LocalDate fetchDate = LocalDate.of(2025, 4, 11);
        String currency = "KZT";
        String pair = "KZT/USD";

        // Мокаем clock
        when(clock.getZone()).thenReturn(ZoneId.of("America/New_York"));
        when(clock.instant()).thenReturn(
            ZonedDateTime.of(2025, 4, 13, 12, 0, 0, 0, ZoneId.of("America/New_York")).toInstant());
        when(clock.withZone(ZoneId.of("America/New_York"))).thenReturn(
            Clock.fixed(
                ZonedDateTime.of(2025, 4, 13, 12, 0, 0, 0, ZoneId.of("America/New_York"))
                    .toInstant(),
                ZoneId.of("America/New_York")));

        when(
            exchangeRateRepository.findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(
                pair, date))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(createRate(pair, "0.0021", fetchDate)));
        when(exchangeRateRepository.findCurrencyPairsByRateDate(fetchDate))
            .thenReturn(Collections.emptyList());

        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("KZT", new BigDecimal("0.0021"));
        when(exchangeRateClient.getHistoricalRates(List.of("KZT"), fetchDate))
            .thenReturn(rates);

        ExchangeRate rate = createRate(pair, "0.0021", fetchDate);
        when(exchangeRateMapper.toEntity(pair, new BigDecimal("0.0021"), fetchDate))
            .thenReturn(rate);
        when(exchangeRateRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        BigDecimal result = exchangeRateService.getRate(currency, date);

        assertEquals(new BigDecimal("0.0021"), result);
        verify(exchangeRateClient).getHistoricalRates(List.of("KZT"), fetchDate);
        verify(exchangeRateRepository).saveAll(anyList());
        verify(exchangeRateMapper).toEntity(pair, new BigDecimal("0.0021"), fetchDate);
        verify(exchangeRateRepository, times(2))
            .findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(pair, date);
    }

    @Test
    void getRate_nonPopularCurrency_fetchesOnlyRequested() {
        LocalDate date = LocalDate.of(2025, 4, 15);
        String currency = "AUD";
        String pair = "AUD/USD";

        // Мокаем clock
        when(clock.getZone()).thenReturn(ZoneId.of("America/New_York"));
        when(clock.instant()).thenReturn(
            ZonedDateTime.of(2025, 4, 15, 18, 0, 0, 0, ZoneId.of("America/New_York")).toInstant());
        when(clock.withZone(ZoneId.of("America/New_York"))).thenReturn(
            Clock.fixed(
                ZonedDateTime.of(2025, 4, 15, 18, 0, 0, 0, ZoneId.of("America/New_York"))
                    .toInstant(),
                ZoneId.of("America/New_York")));

        when(exchangeRateRepository.findCurrencyPairsByRateDate(date))
            .thenReturn(POPULAR_CURRENCIES.stream().map(c -> c + "/USD").toList());
        when(
            exchangeRateRepository.findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(
                pair, date))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(createRate(pair, "0.6500", date)));

        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("AUD", new BigDecimal("0.6500"));
        when(exchangeRateClient.getHistoricalRates(List.of("AUD"), date))
            .thenReturn(rates);

        ExchangeRate rate = createRate(pair, "0.6500", date);
        when(exchangeRateMapper.toEntity(pair, new BigDecimal("0.6500"), date))
            .thenReturn(rate);
        when(exchangeRateRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        BigDecimal result = exchangeRateService.getRate(currency, date);

        assertEquals(new BigDecimal("0.6500"), result);
        verify(exchangeRateClient).getHistoricalRates(List.of("AUD"), date);
        verify(exchangeRateRepository).saveAll(anyList());
        verify(exchangeRateMapper).toEntity(pair, new BigDecimal("0.6500"), date);
        verify(exchangeRateRepository, times(2))
            .findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(pair, date);
    }

    @Test
    void getRate_apiThrowsException_throwsExchangeRateApiException() {
        LocalDate date = LocalDate.of(2025, 4, 15);
        String currency = "KZT";
        String pair = "KZT/USD";

        // Мокаем clock
        when(clock.getZone()).thenReturn(ZoneId.of("America/New_York"));
        when(clock.instant()).thenReturn(
            ZonedDateTime.of(2025, 4, 15, 18, 0, 0, 0, ZoneId.of("America/New_York")).toInstant());
        when(clock.withZone(ZoneId.of("America/New_York"))).thenReturn(
            Clock.fixed(
                ZonedDateTime.of(2025, 4, 15, 18, 0, 0, 0, ZoneId.of("America/New_York"))
                    .toInstant(),
                ZoneId.of("America/New_York")));

        when(
            exchangeRateRepository.findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(
                pair, date))
            .thenReturn(Optional.empty());
        when(exchangeRateRepository.findCurrencyPairsByRateDate(date))
            .thenReturn(Collections.emptyList());

        when(exchangeRateClient.getHistoricalRates(anyList(), eq(date)))
            .thenThrow(new ExchangeRateApiException("API error"));

        ExchangeRateApiException exception = assertThrows(
            ExchangeRateApiException.class,
            () -> exchangeRateService.getRate(currency, date));

        assertEquals("API error", exception.getMessage());
        verify(exchangeRateClient).getHistoricalRates(anyList(), eq(date));
        verify(exchangeRateRepository, never()).saveAll(anyList());
        verifyNoInteractions(exchangeRateMapper);
        verify(exchangeRateRepository)
            .findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(pair, date);
    }

    @Test
    void getRate_rateNotFoundAfterFetch_throwsExchangeRateApiException() {
        LocalDate date = LocalDate.of(2025, 4, 15);
        String currency = "KZT";
        String pair = "KZT/USD";

        // Мокаем clock
        when(clock.getZone()).thenReturn(ZoneId.of("America/New_York"));
        when(clock.instant()).thenReturn(
            ZonedDateTime.of(2025, 4, 15, 18, 0, 0, 0, ZoneId.of("America/New_York")).toInstant());
        when(clock.withZone(ZoneId.of("America/New_York"))).thenReturn(
            Clock.fixed(
                ZonedDateTime.of(2025, 4, 15, 18, 0, 0, 0, ZoneId.of("America/New_York"))
                    .toInstant(),
                ZoneId.of("America/New_York")));

        when(
            exchangeRateRepository.findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(
                pair, date))
            .thenReturn(Optional.empty());
        when(exchangeRateRepository.findCurrencyPairsByRateDate(date))
            .thenReturn(Collections.emptyList());

        // Мокаем вызов с KZT, так как это популярная валюта, но после 17:00 добавятся все POPULAR_CURRENCIES
        Map<String, BigDecimal> rates = new HashMap<>();
        when(exchangeRateClient.getHistoricalRates(POPULAR_CURRENCIES, date))
            .thenReturn(rates);

        ExchangeRateApiException exception = assertThrows(
            ExchangeRateApiException.class,
            () -> exchangeRateService.getRate(currency, date));

        assertEquals("No valid rates received for " + date
                     + ". Please check the currency code for correctness.",
            exception.getMessage());
        verify(exchangeRateClient).getHistoricalRates(POPULAR_CURRENCIES, date);
        verify(exchangeRateRepository, never()).saveAll(anyList());
        verifyNoInteractions(exchangeRateMapper);
        verify(exchangeRateRepository)
            .findTopByCurrencyPairAndRateDateLessThanEqualOrderByRateDateDesc(pair, date);
    }

    private ExchangeRate createRate(String pair, String closeRate, LocalDate date) {
        ExchangeRate rate = new ExchangeRate();
        rate.setCurrencyPair(pair);
        rate.setCloseRate(new BigDecimal(closeRate));
        rate.setRateDate(date);
        return rate;
    }
}