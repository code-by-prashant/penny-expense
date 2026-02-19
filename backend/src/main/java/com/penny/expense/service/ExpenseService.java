package com.penny.expense.service;

import com.penny.expense.dto.CsvUploadResult;
import com.penny.expense.dto.DashboardResponse;
import com.penny.expense.dto.ExpenseRequest;
import com.penny.expense.dto.ExpenseResponse;
import com.penny.expense.mapper.ExpenseMapper;
import com.penny.expense.model.Expense;
import com.penny.expense.repository.ExpenseRepository;
import com.penny.expense.service.strategy.AnomalyDetectionStrategy;
import com.penny.expense.service.strategy.CategorizationStrategy;
import com.penny.expense.service.strategy.DashboardAssembler;
import com.penny.expense.service.strategy.ExpenseFileParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import com.penny.expense.exception.ExpenseNotFoundException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core expense management service — a thin orchestrator.
 *
 * ─── SOLID compliance ─────────────────────────────────────────────────────────
 * S — Single Responsibility: orchestrates CRUD, delegates everything else.
 * O — Open/Closed: all collaborators injected as interfaces; swap impls freely.
 * L — Liskov Substitution: any impl of injected interfaces is substitutable.
 * I — Interface Segregation: each dependency is a narrow, focused interface.
 * D — Dependency Inversion: depends on abstractions, not concrete classes.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRepository        expenseRepository;
    private final ExpenseMapper            expenseMapper;
    private final CategorizationStrategy   categorizationStrategy;
    private final AnomalyDetectionStrategy anomalyDetectionStrategy;
    private final ExpenseFileParser        expenseFileParser;
    private final DashboardAssembler       dashboardAssembler;

    // Read
    
    @Transactional(readOnly = true)
    public List<ExpenseResponse> findAll() {
        return expenseRepository
                .findAll(Sort.by(Sort.Direction.DESC, "date", "id"))
                .stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse findById(Long id) {
        return expenseRepository.findById(id)
                .map(expenseMapper::toResponse)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    // Write

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        String category = categorizationStrategy.categorize(request.getVendorName());
        Expense saved   = expenseRepository.save(expenseMapper.toEntity(request, category));
        anomalyDetectionStrategy.recalculateForCategory(category);
        return expenseMapper.toResponse(
                expenseRepository.findById(saved.getId()).orElseThrow()
        );
    }

    @Transactional
    public void delete(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
        String category = expense.getCategory();
        expenseRepository.deleteById(id);
        anomalyDetectionStrategy.recalculateForCategory(category);
        log.debug("Deleted expense [id={}, category={}]", id, category);
    }

    // CSV Upload

    @Transactional
    public CsvUploadResult uploadFile(MultipartFile file) {
        ExpenseFileParser.ParseResult parsed = expenseFileParser.parse(file);
        if (!parsed.expenses().isEmpty()) {
            expenseRepository.saveAll(parsed.expenses());
            Set<String> affected = parsed.expenses().stream()
                    .map(Expense::getCategory)
                    .collect(Collectors.toSet());
            affected.forEach(anomalyDetectionStrategy::recalculateForCategory);
        }
        log.info("CSV upload: added={}, errors={}", parsed.expenses().size(), parsed.errors().size());
        return CsvUploadResult.builder()
                .added(parsed.expenses().size())
                .failed(parsed.errors().size())
                .errors(parsed.errors())
                .build();
    }

    // Dashboard

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        return dashboardAssembler.assemble();
    }
}
