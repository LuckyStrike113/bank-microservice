package com.bank.repository;

import com.bank.entity.ExpenseCategory;
import com.bank.entity.Transaction;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for managing {@link Transaction} entities.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds all transactions that exceeded their limits.
     *
     * @return a list of transactions with limit_exceeded = true
     */
    @Query("SELECT t FROM Transaction t WHERE t.limitExceeded = true")
    List<Transaction> findAllExceeded();

    @Query("SELECT t FROM Transaction t " +
           "WHERE t.expenseCategory = :category " +
           "AND EXTRACT(YEAR FROM t.datetime) = :year " +
           "AND EXTRACT(MONTH FROM t.datetime) = :month " +
           "AND t.datetime < :date")
    List<Transaction> findAllByCategoryAndMonth(@Param("category") ExpenseCategory category,
        @Param("year") int year,
        @Param("month") int month,
        @Param("date") OffsetDateTime date);

    /**
     * Calculates the total spent amount in USD for a category in a given month up to a specified date.
     *
     * @param category the expense category
     * @param year     the year of the transactions
     * @param month    the month of the transactions
     * @param date     the cutoff date
     * @return the total spent amount in USD
     */
    @Query(value = "SELECT COALESCE(SUM(t.sum * er.close_rate), 0) " +
                   "FROM public.transactions t " +
                   "JOIN public.exchange_rates er " +
                   "ON er.currency_pair = t.currency_shortname || '/USD' " +
                   "WHERE t.expense_category = ?1 " +
                   "AND EXTRACT(YEAR FROM t.datetime) = ?2 " +
                   "AND EXTRACT(MONTH FROM t.datetime) = ?3 " +
                   "AND t.datetime < ?4", nativeQuery = true)
    BigDecimal calculateSpentInMonth(String category, int year, int month, OffsetDateTime date);
}