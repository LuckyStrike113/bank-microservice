package com.bank.client;

import com.bank.dto.ExchangeRateResponse;
import com.bank.exception.ExchangeRateApiException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Client for retrieving exchange rates from Open Exchange Rates API.
 */
@Component
@RequiredArgsConstructor
public class ExchangeRateClient {

    private final RestTemplate restTemplate;

    @Value("${exchange-rate.api.url:https://openexchangerates.org/api}")
    private String baseUrl;

    @Value("${exchange-rate.api.key}")
    private String apiKey;

    /**
     * Fetches historical exchange rates for a list of currencies relative to USD.
     *
     * @param currencies the list of currency codes (e.g., ["KZT", "RUB"])
     * @param date       the date for the rates
     * @return a map of currency codes to their exchange rates (currency/USD)
     * @throws ExchangeRateApiException if the API request fails or returns invalid data
     */
    public Map<String, BigDecimal> getHistoricalRates(List<String> currencies, LocalDate date) {
        String symbols = String.join(",", currencies);
        String url =
            baseUrl + "/historical/" + date + ".json?app_id=" + apiKey + "&symbols=" + symbols;
        try {
            ExchangeRateResponse response = restTemplate.getForObject(url,
                ExchangeRateResponse.class);
            if (response.getRates() == null) {
                throw new ExchangeRateApiException(
                    "Failed to fetch exchange rates from API for " + date + ". Response rates are empty.");
            }
            Map<String, BigDecimal> rates = new HashMap<>();
            for (String currency : currencies) {
                BigDecimal rateToUsd = response.getRates().get(currency);
                if (rateToUsd == null || rateToUsd.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ExchangeRateApiException(
                        "Invalid exchange rate for " + currency + " on " + date + ": " + rateToUsd);
                }
                // Инвертируем: валюта/USD = 1 / (USD/валюта)
                rates.put(currency, BigDecimal.ONE.divide(rateToUsd, 4, RoundingMode.HALF_UP));
            }
            return rates;
        } catch (Exception e) {
            throw new ExchangeRateApiException(
                "Failed to fetch exchange rates from API for " + date +
                ". Please check API key or try again later.", e);
        }
    }
}