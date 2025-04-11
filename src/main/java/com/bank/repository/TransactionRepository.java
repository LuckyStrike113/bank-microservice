package com.bank.repository;

import com.bank.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    /**
     * Finds all transactions that exceeded their limits.
     *
     * @return a list of transactions with limitExceeded set to true
     */
    @Query("SELECT t FROM Transaction t JOIN t.limit l WHERE t.limitExceeded = true")
    List<Transaction> findAllExceeded();

    /**
     * Finds all transactions for a given category and month before a specified date.
     *
     * @param category the expense category
     * @param year the year of the transactions
     * @param month the month of the transactions
     * @param date the upper bound date for transactions
     * @return a list of matching transactions
     */
    @Query("SELECT t FROM Transaction t " +
           "WHERE t.expenseCategory = :category " +
           "AND EXTRACT(YEAR FROM t.datetime) = :year " +
           "AND EXTRACT(MONTH FROM t.datetime) = :month " +
           "AND t.datetime < :date")
    List<Transaction> findAllByCategoryAndMonth(@Param("category") String category,
        @Param("year") int year,
        @Param("month") int month,
        @Param("date") OffsetDateTime date);

    /**
     * Calculates the total spent amount in USD for a category in a given month before a specified date.
     *
     * @param category the expense category
     * @param year the year of the transactions
     * @param month the month of the transactions
     * @param date the upper bound date for transactions
     * @return the total spent amount in USD
     */
    @Query(value = "SELECT COALESCE(SUM(t.sum * er.close_rate), 0) " +
                   "FROM public.transactions t " +
                   "JOIN public.exchange_rates er ON er.currency_pair = t.currency_shortname || '/USD' " +
                   "JOIN public.limits l ON t.limit_id = l.id " +
                   "WHERE t.expense_category = :category " +
                   "AND EXTRACT(YEAR FROM t.datetime) = :year " +
                   "AND EXTRACT(MONTH FROM t.datetime) = :month " +
                   "AND t.datetime < :date",
        nativeQuery = true)
    BigDecimal calculateSpentInMonth(@Param("category") String category,
        @Param("year") int year,
        @Param("month") int month,
        @Param("date") OffsetDateTime date);
}