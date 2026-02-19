package com.penny.expense.service.strategy;

import com.penny.expense.model.Expense;
import com.penny.expense.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mean-multiplier implementation of {@link AnomalyDetectionStrategy}.
 *
 * Rule: an expense is anomalous when its amount exceeds
 *       (category mean) × anomalyMultiplier.
 *
 * OCP: This is one concrete algorithm. An alternative strategy
 * (e.g. Z-score, IQR-based, or time-windowed rolling average) implements
 * AnomalyDetectionStrategy and swaps in — zero changes to ExpenseService.
 *
 * SRP: This class only decides whether expenses are anomalous and persists
 * that decision. It does not know about HTTP, CSV, or dashboard assembly.
 *
 * The anomaly multiplier is externalised to application.properties so it
 * can be changed without recompilation — fulfilling the spirit of OCP at
 * the configuration level too.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MeanMultiplierAnomalyStrategy implements AnomalyDetectionStrategy {

    private final ExpenseRepository expenseRepository;

    @Value("${app.anomaly.multiplier:3.0}")
    private double anomalyMultiplier;

    @Override
    @Transactional
    public void recalculateForCategory(String category) {
        List<Expense> expenses = expenseRepository.findByCategory(category);
        if (expenses.isEmpty()) {
            return;
        }

        double mean = computeMean(expenses);
        double threshold = mean * anomalyMultiplier;

        List<Long> toFlag   = filterIds(expenses, threshold, true);
        List<Long> toUnflag = filterIds(expenses, threshold, false);

        if (!toFlag.isEmpty())   expenseRepository.bulkUpdateAnomalyFlag(toFlag,   true);
        if (!toUnflag.isEmpty()) expenseRepository.bulkUpdateAnomalyFlag(toUnflag, false);

        log.debug("Anomaly recalc [category={}, expenses={}, mean={:.2f}, threshold={:.2f}, flagged={}]",
                category, expenses.size(), mean, threshold, toFlag.size());
    }

    @Override
    public boolean wouldBeAnomaly(String category, BigDecimal amount) {
        BigDecimal avg = expenseRepository.avgAmountByCategory(category);
        if (avg == null || avg.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        return amount.doubleValue() > avg.doubleValue() * anomalyMultiplier;
    }

    // ── Private helpers

    private double computeMean(List<Expense> expenses) {
        return expenses.stream()
                .mapToDouble(e -> e.getAmount().doubleValue())
                .average()
                .orElse(0.0);
    }

    private List<Long> filterIds(List<Expense> expenses, double threshold, boolean exceedsThreshold) {
        return expenses.stream()
                .filter(e -> exceedsThreshold
                        ? e.getAmount().doubleValue() > threshold
                        : e.getAmount().doubleValue() <= threshold)
                .map(Expense::getId)
                .toList();
    }
}
