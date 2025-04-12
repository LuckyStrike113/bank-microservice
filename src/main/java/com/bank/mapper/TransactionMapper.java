package com.bank.mapper;

import com.bank.dto.TransactionRequest;
import com.bank.dto.TransactionResponse;
import com.bank.entity.Limit;
import com.bank.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "currencyShortname", target = "currencyShortname")
    @Mapping(source = "expenseCategory", target = "expenseCategory")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "limitExceeded", ignore = true)
    @Mapping(source = "accountFrom", target = "accountFrom")
    @Mapping(source = "accountTo", target = "accountTo")
    @Mapping(source = "sum", target = "sum")
    @Mapping(source = "datetime", target = "datetime")
    Transaction toEntity(TransactionRequest request);

    @Mapping(source = "currencyShortname", target = "currencyShortname")
    @Mapping(source = "expenseCategory", target = "expenseCategory")
    @Mapping(source = "accountFrom", target = "accountFrom")
    @Mapping(source = "accountTo", target = "accountTo")
    @Mapping(source = "sum", target = "sum")
    @Mapping(source = "datetime", target = "datetime")
    @Mapping(source = "limitExceeded", target = "limitExceeded")
    TransactionResponse toResponse(Transaction transaction);

    @Mapping(source = "transaction.currencyShortname", target = "currencyShortname")
    @Mapping(source = "transaction.expenseCategory", target = "expenseCategory")
    @Mapping(source = "transaction.accountFrom", target = "accountFrom")
    @Mapping(source = "transaction.accountTo", target = "accountTo")
    @Mapping(source = "transaction.sum", target = "sum")
    @Mapping(source = "transaction.datetime", target = "datetime")
    @Mapping(source = "transaction.limitExceeded", target = "limitExceeded")
    @Mapping(source = "limit.limitSum", target = "limitSum", defaultExpression = "java(new java.math.BigDecimal(\"1000.00\"))")
    @Mapping(source = "limit.limitDatetime", target = "limitDatetime", defaultExpression = "java(transaction.getDatetime())")
    @Mapping(target = "limitCurrencyShortname", constant = "USD")
    TransactionResponse toResponse(Transaction transaction, Limit limit);
}