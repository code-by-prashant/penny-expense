package com.penny.expense.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardResponse {

    /** { "2024-01": { "Food": 1200.00, "Transport": 450.00 }, ... } */
    private Map<String, Map<String, BigDecimal>> monthlyByCategory;

    /** Top 5 vendors by total spend */
    private List<VendorStat> topVendors;

    /** All-time spend per category */
    private List<CategoryStat> categoryTotals;

    /** All anomalous expenses */
    private List<ExpenseResponse> anomalies;

    private int anomalyCount;

    @Data @Builder
    public static class VendorStat {
        private String vendorName;
        private BigDecimal total;
        private long count;
    }

    @Data @Builder
    public static class CategoryStat {
        private String category;
        private BigDecimal total;
        private long count;
    }
}
