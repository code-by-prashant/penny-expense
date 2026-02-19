package com.penny.expense.service.strategy;

import com.penny.expense.dto.DashboardResponse;

/**
 * SRP — Single Responsibility Principle:
 * Dashboard data assembly (aggregating monthly totals, building top-vendor
 * lists, collecting anomalies) was all inlined in ExpenseService.getDashboard().
 * That method had multiple reasons to change:
 *   - Business logic changes (e.g. top 10 vendors instead of 5)
 *   - Query strategy changes (e.g. native SQL vs JPQL)
 *   - New dashboard widgets
 *
 * This interface isolates assembly as a dedicated concern.
 * ExpenseService simply delegates to it — one reason to change.
 */
public interface DashboardAssembler {

    /**
     * Build and return the complete dashboard response.
     *
     * @return fully assembled dashboard payload
     */
    DashboardResponse assemble();
}
