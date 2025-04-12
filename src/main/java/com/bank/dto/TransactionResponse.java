package com.bank.dto;

import com.bank.entity.ExpenseCategory;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class TransactionResponse {

    private String accountFrom;
    private String accountTo;
    private String currencyShortname;
    private BigDecimal sum;
    private ExpenseCategory expenseCategory;
    private OffsetDateTime datetime;
    private boolean limitExceeded;
    private BigDecimal limitSum;
    private OffsetDateTime limitDatetime;
    private String limitCurrencyShortname;
}