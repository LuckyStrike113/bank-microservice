package com.bank.dto;

import com.bank.entity.ExpenseCategory;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LimitRequest {

    private BigDecimal limitSum;
    private ExpenseCategory expenseCategory;
}