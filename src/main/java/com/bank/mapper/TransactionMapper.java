package com.bank.mapper;

import com.bank.dto.TransactionRequest;
import com.bank.dto.TransactionResponse;
import com.bank.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between Transaction DTOs and entities.
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    /**
     * Converts a TransactionRequest DTO to a Transaction entity.
     *
     * @param request the transaction request DTO
     * @return the Transaction entity with limitExceeded and limit fields ignored
     */
    @Mapping(target = "limitExceeded", ignore = true)
    @Mapping(target = "limit", ignore = true)
    Transaction toEntity(TransactionRequest request);

    /**
     * Converts a Transaction entity to a TransactionResponse DTO.
     *
     * @param transaction the Transaction entity
     * @return the TransactionResponse DTO with limit details mapped
     */
    @Mapping(source = "limit.limitSum", target = "limitSum")
    @Mapping(source = "limit.limitDatetime", target = "limitDatetime")
    @Mapping(source = "limit.limitCurrencyShortname", target = "limitCurrencyShortname")
    TransactionResponse toResponse(Transaction transaction);
}