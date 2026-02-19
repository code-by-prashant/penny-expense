package com.penny.expense.controller;

import com.penny.expense.dto.CsvUploadResult;
import com.penny.expense.dto.DashboardResponse;
import com.penny.expense.dto.ExpenseRequest;
import com.penny.expense.dto.ExpenseResponse;
import com.penny.expense.service.ExpenseService;
import com.penny.expense.service.strategy.CategorizationStrategy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST controller for expense management.
 *
 * SRP: This class has one job — handle HTTP concerns (routing, status codes,
 * request/response serialization). All business logic is delegated to
 * ExpenseService.
 *
 * DIP: Depends on ExpenseService (a Spring-managed abstraction layer) and
 * CategorizationStrategy (an interface), not on concrete implementations.
 *
 * ISP: The controller only consumes the methods it actually calls — it does
 * not import or reference any strategy implementations directly.
 */
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Broke Era API", description = "Where your money goes to die 😭")
public class ExpenseController {

    private final ExpenseService          expenseService;
    private final CategorizationStrategy  categorizationStrategy;

    @GetMapping
    @Operation(summary = "List all expenses ordered by date desc")
    public List<ExpenseResponse> listAll() {
        return expenseService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single expense by ID")
    public ResponseEntity<ExpenseResponse> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(expenseService.findById(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new expense — auto-categorized by vendor name")
    public ExpenseResponse create(@Valid @RequestBody ExpenseRequest request) {
        return expenseService.create(request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense and re-evaluate anomaly flags for that category")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            expenseService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload-csv")
    @Operation(summary = "Upload a CSV file to bulk-import expenses")
    public ResponseEntity<CsvUploadResult> uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    CsvUploadResult.builder()
                            .added(0).failed(1)
                            .errors(List.of("Uploaded file is empty"))
                            .build()
            );
        }
        return ResponseEntity.ok(expenseService.uploadFile(file));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard: monthly category totals, top vendors, anomaly list")
    public DashboardResponse dashboard() {
        return expenseService.getDashboard();
    }

    @GetMapping("/categories")
    @Operation(summary = "Return the active vendor-to-category rules map")
    public Map<String, String> categories() {
        return categorizationStrategy.getRules();
    }
}
