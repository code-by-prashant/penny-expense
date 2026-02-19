package com.penny.expense.service.strategy;

import java.util.Map;

/**
 * OCP — Open/Closed Principle:
 * Defines the contract for categorization. The system is open for extension
 * (add a new strategy: ML-based, DB-rules-based, etc.) without modifying
 * existing consumers — they all depend on this interface, not a concrete class.
 *
 * ISP — Interface Segregation Principle:
 * This interface is narrow and focused. Consumers that only need to categorize
 * (e.g. ExpenseService, CsvParserService) depend only on categorize().
 * Consumers that need to expose the rules (e.g. the /categories endpoint)
 * depend on getRules(). Both are kept here because they are cohesive:
 * any categorization strategy must be able to explain its rules.
 */
public interface CategorizationStrategy {

    /**
     * Assign a spending category to the given vendor name.
     *
     * @param vendorName raw vendor string (may be null or blank)
     * @return category string, never null; returns "Other" for unmatched input
     */
    String categorize(String vendorName);

    /**
     * Expose the underlying keyword-to-category rules.
     * Used by the /api/expenses/categories endpoint.
     *
     * @return immutable copy of the rules map
     */
    Map<String, String> getRules();
}
