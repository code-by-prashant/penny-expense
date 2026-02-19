package com.penny.expense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// ── Inbound ──────────────────────────────────────────────────────────────────

@Data
public class ExpenseRequest {

    @NotNull(message = "date is required")
    private LocalDate date;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be > 0")
    private BigDecimal amount;

    @NotBlank(message = "vendorName is required")
    private String vendorName;

    private String description;
}
