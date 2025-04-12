package com.bank.repository;

import com.bank.entity.ExpenseCategory;
import com.bank.entity.Limit;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LimitRepository extends JpaRepository<Limit, Long> {

    @Query("SELECT l FROM Limit l " +
           "WHERE l.expenseCategory = :category " +
           "AND l.limitDatetime <= :date " +
           "ORDER BY l.limitDatetime DESC")
    Optional<Limit> findLatestByCategoryBeforeDate(@Param("category") ExpenseCategory category,
        @Param("date") OffsetDateTime date);
}