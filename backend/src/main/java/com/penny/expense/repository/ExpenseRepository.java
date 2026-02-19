package com.penny.expense.repository;

import com.penny.expense.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /** All expenses for a given category (used for anomaly recalculation). */
    List<Expense> findByCategory(String category);

    /** All flagged anomalies, newest first. */
    List<Expense> findByIsAnomalyTrueOrderByAmountDesc();

    /** Average amount per category — used by AnomalyDetectionService. */
    @Query("SELECT AVG(e.amount) FROM Expense e WHERE e.category = :category")
    BigDecimal avgAmountByCategory(@Param("category") String category);

    /** Monthly totals per category: [year-month, category, total, count] */
    @Query("""
        SELECT FUNCTION('TO_CHAR', e.date, 'YYYY-MM') AS month,
               e.category,
               SUM(e.amount)  AS total,
               COUNT(e.id)    AS cnt
        FROM   Expense e
        GROUP  BY FUNCTION('TO_CHAR', e.date, 'YYYY-MM'), e.category
        ORDER  BY month DESC, total DESC
        """)
    List<Object[]> monthlyTotalsByCategory();

    /** H2-compatible version of the above (uses FORMATDATETIME). */
    @Query("""
        SELECT FORMATDATETIME(e.date, 'yyyy-MM') AS month,
               e.category,
               SUM(e.amount)  AS total,
               COUNT(e.id)    AS cnt
        FROM   Expense e
        GROUP  BY FORMATDATETIME(e.date, 'yyyy-MM'), e.category
        ORDER  BY month DESC, total DESC
        """)
    List<Object[]> monthlyTotalsByCategoryH2();

    /** Top N vendors by total spend: [vendorName, total, count] */
    @Query("""
        SELECT e.vendorName, SUM(e.amount) AS total, COUNT(e.id) AS cnt
        FROM   Expense e
        GROUP  BY e.vendorName
        ORDER  BY total DESC
        LIMIT  :limit
        """)
    List<Object[]> topVendors(@Param("limit") int limit);

    /** Bulk-update anomaly flag for a list of IDs. */
    @Modifying
    @Query("UPDATE Expense e SET e.isAnomaly = :flag WHERE e.id IN :ids")
    void bulkUpdateAnomalyFlag(@Param("ids") List<Long> ids, @Param("flag") boolean flag);
}
