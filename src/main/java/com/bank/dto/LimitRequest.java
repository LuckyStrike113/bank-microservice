package com.bank.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class LimitRequest {

    private BigDecimal limitSum;
    private String expenseCategory;
}