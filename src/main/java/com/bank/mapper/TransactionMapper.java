package com.bank.mapper;

import com.bank.dto.TransactionRequest;
import com.bank.dto.TransactionResponse;
import com.bank.entity.Limit;
import com.bank.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper for converting between {@link Transaction}, {@link TransactionRequest}, and
 * {@link TransactionResponse}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {

    /**
     * Converts a {@link TransactionRequest} to a {@link Transaction} entity.
     *
     * @param request the transaction request
     * @return the transaction entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "limitExceeded", ignore = true)
    Transaction toEntity(TransactionRequest request);

    /**
     * Converts a {@link Transaction} entity to a {@link TransactionResponse}, including limit
     * details.
     *
     * @param transaction the transaction entity
     * @param limit       the associated limit entity
     * @return the transaction response
     */
    @Mapping(target = "limitSum", source = "limit.limitSum")
    @Mapping(target = "limitDatetime", source = "limit.limitDatetime")
    @Mapping(target = "limitCurrencyShortname", constant = "USD")
    @Mapping(target = "expenseCategory", source = "transaction.expenseCategory")
    TransactionResponse toResponse(Transaction transaction, Limit limit);
}