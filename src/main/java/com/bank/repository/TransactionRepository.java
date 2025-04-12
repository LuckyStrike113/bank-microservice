package com.bank.repository;

import com.bank.entity.ExpenseCategory;
import com.bank.entity.Transaction;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t JOIN Limit l ON t.expenseCategory = l.expenseCategory " +
           "WHERE t.limitExceeded = true AND l.limitDatetime <= t.datetime")
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

    @Query(value = "SELECT COALESCE(SUM(t.sum * er.close_rate), 0) " +
                   "FROM public.transactions t " +
                   "JOIN public.exchange_rates er ON er.currency_pair = t.currency_shortname || '/USD' "
                   +
                   "WHERE t.expense_category = :category " +
                   "AND EXTRACT(YEAR FROM t.datetime) = :year " +
                   "AND EXTRACT(MONTH FROM t.datetime) = :month " +
                   "AND t.datetime < :date",
        nativeQuery = true)
    BigDecimal calculateSpentInMonth(@Param("category") ExpenseCategory category,
        @Param("year") int year,
        @Param("month") int month,
        @Param("date") OffsetDateTime date);
}