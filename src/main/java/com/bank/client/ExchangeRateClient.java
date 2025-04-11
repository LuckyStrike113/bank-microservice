package com.bank.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Client for retrieving exchange rates from an external API (Open Exchange Rates).
 */
@Component
public class ExchangeRateClient {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Retrieves the exchange rate for a given currency relative to USD.
     *
     * @param currency the currency code (e.g., "KZT", "EUR")
     * @return the exchange rate (e.g., KZT/USD rate)
     * @throws RuntimeException if the API request fails or the rate is not found
     */
    public BigDecimal getRate(String currency) {
        if (currency.equals("USD")) {
            return BigDecimal.ONE; // Если валюта USD, курс 1
        }

        String apiKey = "a663ddf94b9945398a68ecaa9d359361"; // Заменить на реальный ключ
        String baseUrl = "https://openexchangerates.org/api/latest.json";
        String url = baseUrl + "?app_id=" + apiKey;

        try {
            ExchangeRateResponse response = restTemplate.getForObject(url, ExchangeRateResponse.class);
            if (response.getRates() == null) {
                throw new RuntimeException("Failed to fetch exchange rates");
            }
            BigDecimal rateToUsd = response.getRates().get(currency);
            if (rateToUsd == null) {
                throw new RuntimeException("Rate for " + currency + " not found");
            }
            // Инвертируем курс: KZT/USD = 1 / (USD/KZT)
            return BigDecimal.ONE.divide(rateToUsd, 6, RoundingMode.HALF_UP);
        } catch (Exception e) {
            throw new RuntimeException(
                "Error fetching rate for " + currency + ": " + e.getMessage());
        }
    }
}