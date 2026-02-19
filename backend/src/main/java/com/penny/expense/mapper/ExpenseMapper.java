package com.penny.expense.mapper;

import com.penny.expense.dto.ExpenseRequest;
import com.penny.expense.dto.ExpenseResponse;
import com.penny.expense.model.Expense;
import org.springframework.stereotype.Component;

/**
 * SRP — Single Responsibility Principle:
 * This class has one job: convert between the Expense entity and its DTOs.
 * Previously, ExpenseResponse.from(Expense) lived on the DTO itself,
 * coupling the DTO to the entity. A dedicated mapper decouples them.
 */
@Component
public class ExpenseMapper {

    public ExpenseResponse toResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .date(expense.getDate())
                .amount(expense.getAmount())
                .vendorName(expense.getVendorName())
                .description(expense.getDescription())
                .category(expense.getCategory())
                .isAnomaly(expense.isAnomaly())
                .createdAt(expense.getCreatedAt())
                .build();
    }

    public Expense toEntity(ExpenseRequest request, String category) {
        return Expense.builder()
                .date(request.getDate())
                .amount(request.getAmount())
                .vendorName(request.getVendorName())
                .description(request.getDescription() == null ? "" : request.getDescription().trim())
                .category(category)
                .build();
    }
}
