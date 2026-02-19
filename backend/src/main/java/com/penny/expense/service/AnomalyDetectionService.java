package com.penny.expense.service;

import com.penny.expense.repository.ExpenseRepository;
import com.penny.expense.service.strategy.AnomalyDetectionStrategy;
import com.penny.expense.service.strategy.MeanMultiplierAnomalyStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that exposes the active AnomalyDetectionStrategy bean.
 *
 * OCP + DIP: ExpenseService depends on AnomalyDetectionStrategy interface.
 * Swap to a Z-score or IQR-based strategy by returning a different impl here.
 */
@Configuration
public class AnomalyDetectionService {

    @Bean
    public AnomalyDetectionStrategy anomalyDetectionStrategy(ExpenseRepository expenseRepository) {
        return new MeanMultiplierAnomalyStrategy(expenseRepository);
    }
}
