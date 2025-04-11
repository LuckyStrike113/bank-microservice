package com.bank;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bank.client.ExchangeRateClient;
import com.bank.dto.TransactionRequest;
import com.bank.entity.ExchangeRate;
import com.bank.entity.Limit;
import com.bank.entity.Transaction;
import com.bank.mapper.TransactionMapper;
import com.bank.repository.ExchangeRateRepository;
import com.bank.repository.LimitRepository;
import com.bank.repository.TransactionRepository;
import com.bank.service.TransactionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LimitRepository limitRepository;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @Mock
    private TransactionMapper transactionMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processTransaction_setsLimitExceededCorrectly() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountFrom("0000000123");
        request.setAccountTo("9999999999");
        request.setCurrencyShortname("KZT");
        request.setSum(BigDecimal.valueOf(10000));
        request.setExpenseCategory("product");
        request.setDatetime(OffsetDateTime.now());

        Limit limit = new Limit();
        limit.setId(1L);
        limit.setLimitSum(BigDecimal.valueOf(1000));
        limit.setLimitDatetime(OffsetDateTime.now());
        limit.setLimitCurrencyShortname("USD");
        limit.setExpenseCategory("product");

        ExchangeRate rate = new ExchangeRate();
        rate.setCurrencyPair("KZT/USD");
        rate.setCloseRate(BigDecimal.valueOf(0.0021));
        rate.setDate(LocalDate.now());

        Transaction transaction = new Transaction();
        transaction.setAccountFrom(request.getAccountFrom());
        transaction.setAccountTo(request.getAccountTo());
        transaction.setCurrencyShortname(request.getCurrencyShortname());
        transaction.setSum(request.getSum());
        transaction.setExpenseCategory(request.getExpenseCategory());
        transaction.setDatetime(request.getDatetime());

        when(limitRepository.findLatestByCategoryBeforeDate("product", request.getDatetime()))
            .thenReturn(Optional.of(limit));
        when(exchangeRateRepository.findLatestByPairAndDate("KZT/USD", LocalDate.now())).thenReturn(
            Optional.of(rate));
        when(exchangeRateClient.getRate("KZT")).thenReturn(BigDecimal.valueOf(0.0021));
        when(transactionRepository.calculateSpentInMonth("product", request.getDatetime().getYear(),
            request.getDatetime().getMonthValue(), request.getDatetime())).thenReturn(
            BigDecimal.ZERO);
        when(transactionMapper.toEntity(request)).thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(
            invocation -> invocation.getArgument(0));

        transactionService.processTransaction(request);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getValue();

        assertFalse(savedTransaction.isLimitExceeded(), "limit_exceeded should be false");
    }

    @Test
    void processTransaction_setsLimitExceededWhenExceeded() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountFrom("0000000123");
        request.setAccountTo("9999999999");
        request.setCurrencyShortname("USD");
        request.setSum(BigDecimal.valueOf(600));
        request.setExpenseCategory("product");
        request.setDatetime(OffsetDateTime.parse("2022-01-03T10:00:00Z"));

        Limit limit = new Limit();
        limit.setId(1L);
        limit.setLimitSum(BigDecimal.valueOf(1000));
        limit.setLimitDatetime(OffsetDateTime.parse("2022-01-01T00:00:00Z"));
        limit.setLimitCurrencyShortname("USD");
        limit.setExpenseCategory("product");

        ExchangeRate rate = new ExchangeRate();
        rate.setCurrencyPair("USD/USD");
        rate.setCloseRate(BigDecimal.ONE);
        rate.setDate(LocalDate.parse("2022-01-03"));

        Transaction transaction = new Transaction();
        transaction.setAccountFrom(request.getAccountFrom());
        transaction.setAccountTo(request.getAccountTo());
        transaction.setCurrencyShortname(request.getCurrencyShortname());
        transaction.setSum(request.getSum());
        transaction.setExpenseCategory(request.getExpenseCategory());
        transaction.setDatetime(request.getDatetime());

        when(limitRepository.findLatestByCategoryBeforeDate("product", request.getDatetime()))
            .thenReturn(Optional.of(limit));
        when(exchangeRateRepository.findLatestByPairAndDate("USD/USD",
            LocalDate.parse("2022-01-03")))
            .thenReturn(Optional.of(rate));
        when(exchangeRateClient.getRate("USD")).thenReturn(BigDecimal.ONE);
        when(transactionRepository.calculateSpentInMonth("product", 2022, 1, request.getDatetime()))
            .thenReturn(BigDecimal.valueOf(500));
        when(transactionMapper.toEntity(request)).thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(
            invocation -> invocation.getArgument(0));

        transactionService.processTransaction(request);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getValue();

        assertTrue(savedTransaction.isLimitExceeded(), "limit_exceeded should be true");
    }
}