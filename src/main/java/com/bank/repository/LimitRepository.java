package com.bank.repository;

import com.bank.entity.ExpenseCategory;
import com.bank.entity.Limit;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing {@link Limit} entities.
 */
@Repository
public interface LimitRepository extends JpaRepository<Limit, Long> {

    /**
     * Finds the latest limit for a category before or on a given date.
     *
     * @param category the expense category
     * @param date     the cutoff date
     * @return an optional containing the latest limit, or empty if none found
     */
    @Query("SELECT l FROM Limit l " +
           "WHERE l.expenseCategory = :category AND l.limitDatetime <= :date " +
           "ORDER BY l.limitDatetime DESC")
    Optional<Limit> findLatestByCategoryBeforeDate(ExpenseCategory category, OffsetDateTime date);
}