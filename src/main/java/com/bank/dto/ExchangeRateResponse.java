package com.bank.dto;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ExchangeRateResponse {

    private Map<String, BigDecimal> rates;
}
