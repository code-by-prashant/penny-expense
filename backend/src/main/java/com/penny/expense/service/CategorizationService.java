package com.penny.expense.service;

import com.penny.expense.service.strategy.CategorizationStrategy;
import com.penny.expense.service.strategy.KeywordCategorizationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that exposes the active CategorizationStrategy bean.
 *
 * OCP + DIP: ExpenseService and CsvExpenseParser depend on the
 * CategorizationStrategy interface. To swap algorithms, change the
 * @Bean method here — zero changes to any consumer.
 *
 * This replaces the old @Service CategorizationService class which:
 *   1. Mixed strategy logic with Spring bean registration
 *   2. Was a concrete class that consumers depended on directly
 */
@Configuration
public class CategorizationService {

    @Bean
    public CategorizationStrategy categorizationStrategy() {
        return new KeywordCategorizationStrategy();
    }
}
