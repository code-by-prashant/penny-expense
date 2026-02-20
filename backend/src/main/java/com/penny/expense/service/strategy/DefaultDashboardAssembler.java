package com.penny.expense.service.strategy;

import com.penny.expense.dto.DashboardResponse;
import com.penny.expense.dto.ExpenseResponse;
import com.penny.expense.mapper.ExpenseMapper;
import com.penny.expense.model.Expense;
import com.penny.expense.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DefaultDashboardAssembler implements DashboardAssembler {

    private static final int TOP_VENDORS_LIMIT = 5;

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper     expenseMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse assemble() {
        List<Expense> all = expenseRepository.findAll(
                Sort.by(Sort.Direction.DESC, "date"));

        List<ExpenseResponse> anomalies =
                expenseRepository.findByIsAnomalyTrueOrderByAmountDesc()
                        .stream()
                        .map(expenseMapper::toResponse)
                        .toList();

        return DashboardResponse.builder()
                .monthlyByCategory(buildMonthlyByCategory(all))
                .topVendors(buildTopVendors(all))
                .categoryTotals(buildCategoryTotals(all))
                .anomalies(anomalies)
                .anomalyCount(anomalies.size())
                .build();
    }

    private Map<String, Map<String, BigDecimal>> buildMonthlyByCategory(List<Expense> expenses) {
        Map<String, Map<String, BigDecimal>> result = new LinkedHashMap<>();
        for (Expense e : expenses) {
            String month = String.format("%d-%02d",
                    e.getDate().getYear(), e.getDate().getMonthValue());
            result.computeIfAbsent(month, k -> new LinkedHashMap<>())
                    .merge(e.getCategory(), e.getAmount(), BigDecimal::add);
        }
        return result;
    }

    private List<DashboardResponse.CategoryStat> buildCategoryTotals(List<Expense> expenses) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        Map<String, Long>       counts = new LinkedHashMap<>();
        for (Expense e : expenses) {
            totals.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
            counts.merge(e.getCategory(), 1L, Long::sum);
        }
        return totals.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(entry -> DashboardResponse.CategoryStat.builder()
                        .category(entry.getKey())
                        .total(entry.getValue())
                        .count(counts.getOrDefault(entry.getKey(), 0L))
                        .build())
                .toList();
    }

    private List<DashboardResponse.VendorStat> buildTopVendors(List<Expense> expenses) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        Map<String, Long>       counts = new LinkedHashMap<>();
        for (Expense e : expenses) {
            totals.merge(e.getVendorName(), e.getAmount(), BigDecimal::add);
            counts.merge(e.getVendorName(), 1L, Long::sum);
        }
        return totals.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(TOP_VENDORS_LIMIT)
                .map(entry -> DashboardResponse.VendorStat.builder()
                        .vendorName(entry.getKey())
                        .total(entry.getValue())
                        .count(counts.getOrDefault(entry.getKey(), 0L))
                        .build())
                .toList();
    }
}