package com.penny.expense.service.strategy;

import com.penny.expense.dto.DashboardResponse;
import com.penny.expense.mapper.ExpenseMapper;
import com.penny.expense.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link DashboardAssembler}.
 *
 * SRP: This class has one job — query the repository and assemble
 * the DashboardResponse. Previously this was spread across multiple
 * private methods inside ExpenseService, giving that service multiple
 * reasons to change (new widgets, new queries, different aggregation).
 *
 * DIP — Dependency Inversion Principle:
 * Depends on ExpenseRepository (an abstraction / Spring Data interface),
 * not on any concrete JDBC or SQL implementation.
 *
 * The dual H2/PostgreSQL query fallback lives here, not in the service —
 * it is a persistence concern, not a business logic concern.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultDashboardAssembler implements DashboardAssembler {

    private static final int TOP_VENDORS_LIMIT = 5;

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper     expenseMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse assemble() {
        List<Object[]> monthlyRaw = fetchMonthlyRaw();

        Map<String, Map<String, BigDecimal>> monthlyByCategory = buildMonthlyByCategory(monthlyRaw);
        List<DashboardResponse.CategoryStat> categoryTotals    = buildCategoryTotals(monthlyRaw);
        List<DashboardResponse.VendorStat>   topVendors        = buildTopVendors();
        var anomalies = expenseRepository.findByIsAnomalyTrueOrderByAmountDesc()
                .stream()
                .map(expenseMapper::toResponse)
                .toList();

        return DashboardResponse.builder()
                .monthlyByCategory(monthlyByCategory)
                .topVendors(topVendors)
                .categoryTotals(categoryTotals)
                .anomalies(anomalies)
                .anomalyCount(anomalies.size())
                .build();
    }

    /**
     * Try the H2-compatible query first (dev profile).
     * Fall back to the PostgreSQL TO_CHAR version (prod profile).
     */
    private List<Object[]> fetchMonthlyRaw() {
        try {
            return expenseRepository.monthlyTotalsByCategoryH2();
        } catch (Exception e) {
            log.debug("H2 monthly query failed, falling back to PostgreSQL query: {}", e.getMessage());
            return expenseRepository.monthlyTotalsByCategory();
        }
    }

    private Map<String, Map<String, BigDecimal>> buildMonthlyByCategory(List<Object[]> rows) {
        Map<String, Map<String, BigDecimal>> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String month    = (String) row[0];
            String category = (String) row[1];
            BigDecimal total = toBigDecimal(row[2]);
            result.computeIfAbsent(month, k -> new LinkedHashMap<>()).put(category, total);
        }
        return result;
    }

    private List<DashboardResponse.CategoryStat> buildCategoryTotals(List<Object[]> rows) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        Map<String, Long> counts       = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String cat     = (String) row[1];
            BigDecimal val = toBigDecimal(row[2]);
            long cnt       = ((Number) row[3]).longValue();
            totals.merge(cat, val, BigDecimal::add);
            counts.merge(cat, cnt, Long::sum);
        }
        return totals.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(e -> DashboardResponse.CategoryStat.builder()
                        .category(e.getKey())
                        .total(e.getValue())
                        .count(counts.getOrDefault(e.getKey(), 0L))
                        .build())
                .toList();
    }

    private List<DashboardResponse.VendorStat> buildTopVendors() {
        return expenseRepository.topVendors(TOP_VENDORS_LIMIT).stream()
                .map(r -> DashboardResponse.VendorStat.builder()
                        .vendorName((String) r[0])
                        .total(toBigDecimal(r[1]))
                        .count(((Number) r[2]).longValue())
                        .build())
                .toList();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bd) return bd;
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }
}
