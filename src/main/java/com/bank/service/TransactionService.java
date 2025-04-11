package com.bank.service;

import com.bank.client.ExchangeRateClient;
import com.bank.dto.TransactionRequest;
import com.bank.dto.TransactionResponse;
import com.bank.entity.ExchangeRate;
import com.bank.entity.Limit;
import com.bank.entity.Transaction;
import com.bank.mapper.TransactionMapper;
import com.bank.repository.ExchangeRateRepository;
import com.bank.repository.LimitRepository;
import com.bank.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for processing bank transactions and managing exceeded limits.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final LimitRepository limitRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateClient exchangeRateClient;
    private final TransactionMapper transactionMapper;

    /**
     * Processes a transaction request, calculates its value in USD, and determines if it exceeds the limit.
     *
     * @param request the transaction request containing account details, sum, currency, and category
     * @throws RuntimeException if exchange rate retrieval fails or database operations encounter errors
     */
    @Transactional
    public void processTransaction(TransactionRequest request) {
        Limit latestLimit = limitRepository.findLatestByCategoryBeforeDate(
                request.getExpenseCategory(), request.getDatetime())
            .orElseGet(() -> createDefaultLimit(request.getExpenseCategory()));

        BigDecimal rate = getExchangeRate(request.getCurrencyShortname(), request.getDatetime().toLocalDate());
        BigDecimal sumInUsd = request.getSum().multiply(rate, new MathContext(2, RoundingMode.HALF_UP));

        BigDecimal spentInMonth = calculateSpentInMonth(request.getExpenseCategory(), request.getDatetime());
        boolean limitExceeded = spentInMonth.add(sumInUsd).compareTo(latestLimit.getLimitSum()) > 0;

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setLimitExceeded(limitExceeded);
        transaction.setLimit(latestLimit);
        transactionRepository.save(transaction);
    }

    /**
     * Retrieves a list of transactions that exceeded their respective limits.
     *
     * @return a list of TransactionResponse objects representing exceeded transactions
     */
    public List<TransactionResponse> getExceededTransactions() {
        return transactionRepository.findAllExceeded().stream()
            .map(transactionMapper::toResponse)
            .toList();
    }

    /**
     * Calculates the total spent amount in USD for a category in the given month up to the specified date.
     *
     * @param category the expense category (e.g., "product", "service")
     * @param date the date up to which transactions are considered
     * @return the total spent amount in USD, or zero if no transactions exist
     */
    private BigDecimal calculateSpentInMonth(String category, OffsetDateTime date) {
        BigDecimal result = transactionRepository.calculateSpentInMonth(category, date.getYear(),
            date.getMonthValue(), date);
        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * Retrieves the exchange rate for a given currency on a specific date, caching it if not already present.
     *
     * @param currency the currency code (e.g., "KZT", "USD")
     * @param date the date for which the rate is needed
     * @return the exchange rate relative to USD
     */
    private BigDecimal getExchangeRate(String currency, LocalDate date) {
        String pair = currency + "/USD";
        return exchangeRateRepository.findLatestByPairAndDate(pair, date)
            .map(ExchangeRate::getCloseRate)
            .orElseGet(() -> {
                BigDecimal rate = exchangeRateClient.getRate(currency);
                ExchangeRate newRate = new ExchangeRate();
                newRate.setCurrencyPair(pair);
                newRate.setCloseRate(rate);
                newRate.setDate(date);
                exchangeRateRepository.save(newRate);
                return rate;
            });
    }

    /**
     * Creates a default limit for a category if no existing limit is found.
     *
     * @param category the expense category for the limit
     * @return the newly created and saved Limit entity
     */
    private Limit createDefaultLimit(String category) {
        Limit limit = new Limit();
        limit.setLimitSum(BigDecimal.valueOf(1000));
        limit.setLimitDatetime(OffsetDateTime.now());
        limit.setLimitCurrencyShortname("USD");
        limit.setExpenseCategory(category);
        return limitRepository.save(limit);
    }
}