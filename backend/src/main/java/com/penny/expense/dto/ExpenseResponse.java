package com.penny.expense.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Outbound response DTO for a single expense.
 *
 * SRP: This is a pure data carrier — it holds fields and nothing else.
 * The static factory method from(Expense) has been removed and moved to
 * ExpenseMapper, which is the correct place for entity↔DTO conversion.
 * A DTO should not know about the entity it represents.
 */
@Data
@Builder
public class ExpenseResponse {
    private Long          id;
    private LocalDate     date;
    private BigDecimal    amount;
    private String        vendorName;
    private String        description;
    private String        category;
    private boolean       isAnomaly;
    private LocalDateTime createdAt;
}
