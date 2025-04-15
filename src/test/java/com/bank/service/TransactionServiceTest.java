package com.bank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.bank.dto.TransactionRequest;
import com.bank.dto.TransactionResponse;
import com.bank.entity.ExpenseCategory;
import com.bank.entity.Limit;
import com.bank.entity.Transaction;
import com.bank.mapper.TransactionMapper;
import com.bank.repository.LimitRepository;
import com.bank.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LimitRepository limitRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private TransactionRequest request;
    private Transaction transaction;
    private Limit limit;
    private TransactionResponse response;

    @BeforeEach
    void setUp() {
        request = new TransactionRequest();
        request.setAccountFrom("1234567890");
        request.setAccountTo("0987654321");
        request.setCurrencyShortname("KZT");
        request.setSum(new BigDecimal("10000.00"));
        request.setExpenseCategory(ExpenseCategory.PRODUCT);
        request.setDatetime(OffsetDateTime.now().minusHours(1));

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccountFrom("1234567890");
        transaction.setAccountTo("0987654321");
        transaction.setCurrencyShortname("KZT");
        transaction.setSum(new BigDecimal("10000.00"));
        transaction.setExpenseCategory(ExpenseCategory.PRODUCT);
        transaction.setDatetime(request.getDatetime());
        transaction.setLimitExceeded(false);

        limit = new Limit();
        limit.setLimitSum(new BigDecimal("1000.00"));
        limit.setLimitDatetime(OffsetDateTime.now());
        limit.setExpenseCategory(ExpenseCategory.PRODUCT);

        response = new TransactionResponse();
        response.setAccountFrom("1234567890");
        response.setAccountTo("0987654321");
        response.setCurrencyShortname("KZT");
        response.setSum(new BigDecimal("10000.00"));
        response.setExpenseCategory(ExpenseCategory.PRODUCT);
        response.setDatetime(request.getDatetime());
        response.setLimitExceeded(false);
        response.setLimitSum(new BigDecimal("1000.00"));
        response.setLimitDatetime(limit.getLimitDatetime());
        response.setLimitCurrencyShortname("USD");
    }

    @Test
    void processTransaction_validRequest_savesAndReturnsResponse() {
        when(limitRepository.findLatestByCategoryBeforeDate(any(ExpenseCategory.class),
            any(OffsetDateTime.class)))
            .thenReturn(Optional.of(limit));
        when(exchangeRateService.getRate("KZT", request.getDatetime().toLocalDate()))
            .thenReturn(new BigDecimal("0.0021"));
        when(transactionMapper.toEntity(any(TransactionRequest.class))).thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toResponse(any(Transaction.class), any(Limit.class))).thenReturn(
            response);

        TransactionResponse result = transactionService.processTransaction(request);

        assertNotNull(result);
        assertEquals("1234567890", result.getAccountFrom());
        assertEquals("KZT", result.getCurrencyShortname());
        assertEquals(new BigDecimal("1000.00"), result.getLimitSum());
        assertEquals("USD", result.getLimitCurrencyShortname());
        assertFalse(result.isLimitExceeded());
    }

    @Test
    void processTransaction_noLimit_createsDefault() {
        when(limitRepository.findLatestByCategoryBeforeDate(any(ExpenseCategory.class),
            any(OffsetDateTime.class)))
            .thenReturn(Optional.empty());
        when(exchangeRateService.getRate("KZT", request.getDatetime().toLocalDate()))
            .thenReturn(new BigDecimal("0.0021"));
        when(transactionMapper.toEntity(any(TransactionRequest.class))).thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(limitRepository.save(any(Limit.class))).thenReturn(limit);
        when(transactionMapper.toResponse(any(Transaction.class), any(Limit.class))).thenReturn(
            response);

        TransactionResponse result = transactionService.processTransaction(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("1000.00"), result.getLimitSum());
    }

    @Test
    void processTransaction_noExchangeRate_throwsException() {
        when(limitRepository.findLatestByCategoryBeforeDate(any(ExpenseCategory.class),
            any(OffsetDateTime.class)))
            .thenReturn(Optional.of(limit));
        when(exchangeRateService.getRate("KZT", request.getDatetime().toLocalDate()))
            .thenThrow(new IllegalArgumentException("No rate for KZT/USD"));

        assertThrows(IllegalArgumentException.class,
            () -> transactionService.processTransaction(request));
    }
}