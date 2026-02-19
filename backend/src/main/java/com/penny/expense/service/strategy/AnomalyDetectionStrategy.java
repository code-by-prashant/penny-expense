package com.penny.expense.service.strategy;

import java.math.BigDecimal;

/**
 * OCP — Open/Closed Principle:
 * Consumers of anomaly detection (ExpenseService) depend on this interface.
 * To change the detection algorithm (e.g. from mean×3 to standard deviation,
 * or to a time-windowed rolling average), implement a new class and swap the
 * Spring bean — zero changes to ExpenseService required.
 *
 * ISP — Interface Segregation Principle:
 * Two focused methods rather than one bloated interface:
 *   - recalculateForCategory: used after write operations
 *   - wouldBeAnomaly: used for preview/hints without persisting
 */
public interface AnomalyDetectionStrategy {

    /**
     * Re-evaluate and persist the anomaly flag for every expense in
     * the given category. Called after every insert and delete.
     *
     * @param category the category to rescan
     */
    void recalculateForCategory(String category);

    /**
     * Predict — without persisting anything — whether a given amount
     * would be flagged as anomalous for a category given current data.
     *
     * @param category spending category
     * @param amount   proposed amount
     * @return true if the amount would exceed the anomaly threshold
     */
    boolean wouldBeAnomaly(String category, BigDecimal amount);
}
