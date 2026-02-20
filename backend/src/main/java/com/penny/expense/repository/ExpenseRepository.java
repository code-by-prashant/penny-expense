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

    List<Expense> findByCategory(String category);

    List<Expense> findByIsAnomalyTrueOrderByAmountDesc();

    @Query("SELECT AVG(e.amount) FROM Expense e WHERE e.category = :category")
    BigDecimal avgAmountByCategory(@Param("category") String category);

    @Modifying
    @Query("UPDATE Expense e SET e.isAnomaly = :flag WHERE e.id IN :ids")
    void bulkUpdateAnomalyFlag(@Param("ids") List<Long> ids, @Param("flag") boolean flag);
}