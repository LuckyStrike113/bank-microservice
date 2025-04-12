package com.bank.dto;

import com.bank.entity.ExpenseCategory;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    private String accountFrom;
    private String accountTo;
    private String currencyShortname;
    private BigDecimal sum;
    private ExpenseCategory expenseCategory;
    private OffsetDateTime datetime;
}
