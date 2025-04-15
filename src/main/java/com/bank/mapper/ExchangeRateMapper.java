package com.bank.mapper;

import com.bank.entity.ExchangeRate;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for ExchangeRate entities.
 */
@Mapper(componentModel = "spring")
public interface ExchangeRateMapper {

    /**
     * Maps currency pair, rate, and date to ExchangeRate entity.
     *
     * @param currencyPair the currency pair (e.g., "KZT/USD")
     * @param rate         the exchange rate
     * @param rateDate     the date of the rate
     * @return ExchangeRate entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currencyPair", source = "currencyPair")
    @Mapping(target = "closeRate", source = "rate")
    @Mapping(target = "rateDate", source = "rateDate")
    ExchangeRate toEntity(String currencyPair, BigDecimal rate, LocalDate rateDate);
}