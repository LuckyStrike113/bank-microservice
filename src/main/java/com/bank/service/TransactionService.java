package com.bank.service;

import com.bank.dto.TransactionRequest;
import com.bank.dto.TransactionResponse;
import com.bank.entity.ExpenseCategory;
import com.bank.entity.Limit;
import com.bank.entity.Transaction;
import com.bank.mapper.TransactionMapper;
import com.bank.repository.LimitRepository;
import com.bank.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for processing bank transactions, calculating their value in USD, and managing limit
 * exceedances.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final LimitRepository limitRepository;
    private final ExchangeRateService exchangeRateService;
    private final TransactionMapper transactionMapper;

    /**
     * Processes a transaction request, converts its value to USD, checks if it exceeds the limit,
     * and saves it to the database.
     *
     * @param request the transaction request containing account details, sum, currency, category,
     *                and datetime
     * @return the saved transaction details as a {@link TransactionResponse}
     * @throws IllegalArgumentException if the exchange rate cannot be retrieved
     */
    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {
        log.info("Processing transaction for accountFrom: {}, sum: {}, currency: {}",
            request.getAccountFrom(), request.getSum(), request.getCurrencyShortname());

        Limit latestLimit = limitRepository.findLatestByCategoryBeforeDate(
                request.getExpenseCategory(), request.getDatetime())
            .orElseGet(() -> createDefaultLimit(request.getExpenseCategory()));

        BigDecimal rate = exchangeRateService.getRate(
            request.getCurrencyShortname(), request.getDatetime().toLocalDate());
        BigDecimal sumInUsd = request.getSum()
            .multiply(rate, new MathContext(4, RoundingMode.HALF_UP));

        BigDecimal spentInMonth = calculateSpentInMonth(request.getExpenseCategory(),
            request.getDatetime());
        boolean limitExceeded = spentInMonth.add(sumInUsd).compareTo(latestLimit.getLimitSum()) > 0;

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setLimitExceeded(limitExceeded);
        transaction = transactionRepository.save(transaction);

        log.info("Transaction saved with id: {}, limitExceeded: {}", transaction.getId(),
            limitExceeded);
        return transactionMapper.toResponse(transaction, latestLimit);
    }

    /**
     * Retrieves a list of transactions that exceeded their respective limits.
     *
     * @return a list of {@link TransactionResponse} objects representing exceeded transactions
     */
    public List<TransactionResponse> getExceededTransactions() {
        return transactionRepository.findAllExceeded().stream()
            .map(transaction -> transactionMapper.toResponse(transaction,
                limitRepository.findLatestByCategoryBeforeDate(
                    transaction.getExpenseCategory(), transaction.getDatetime()
                ).orElse(null)))
            .toList();
    }

    /**
     * Calculates the total spent amount in USD for a category in the given month up to the
     * specified date.
     *
     * @param category the expense category ({@link ExpenseCategory#PRODUCT} or
     *                 {@link ExpenseCategory#SERVICE})
     * @param date     the date up to which transactions are considered
     * @return the total spent amount in USD, or zero if no transactions exist
     */
    private BigDecimal calculateSpentInMonth(ExpenseCategory category, OffsetDateTime date) {
        BigDecimal result = transactionRepository.calculateSpentInMonth(
            category.name(), date.getYear(), date.getMonthValue(), date);
        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * Creates a default limit for a category if no existing limit is found.
     *
     * @param category the expense category for the limit
     * @return the newly created and saved {@link Limit} entity
     */
    private Limit createDefaultLimit(ExpenseCategory category) {
        Limit limit = new Limit();
        limit.setLimitSum(BigDecimal.valueOf(1000));
        limit.setLimitDatetime(OffsetDateTime.now());
        limit.setExpenseCategory(category);
        return limitRepository.save(limit);
    }
}