package com.penny.expense.service.strategy;

import com.penny.expense.dto.CsvUploadResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * SRP — Single Responsibility Principle:
 * CSV parsing was buried inside ExpenseService.uploadCsv(), mixing:
 *   1. File I/O and row parsing
 *   2. Validation
 *   3. Entity construction
 *   4. Saving to DB
 *   5. Triggering anomaly recalculation
 *
 * This interface extracts responsibility #1-3 into a dedicated parser.
 * ExpenseService retains #4-5 which are its genuine concern.
 *
 * OCP — Open/Closed Principle:
 * New file formats (Excel, JSON, OFX bank exports) can be added by
 * implementing this interface — no changes to ExpenseService needed.
 */
public interface ExpenseFileParser {

    /**
     * Parse the uploaded file and return a result containing successfully
     * built expenses ready for persistence plus any per-row errors.
     *
     * @param file the uploaded multipart file
     * @return parse result with expense data and errors
     */
    ParseResult parse(MultipartFile file);

    /**
     * Value object carrying the parse output.
     * Keeps raw Expense data separate from error reporting.
     */
    record ParseResult(
            java.util.List<com.penny.expense.model.Expense> expenses,
            java.util.List<String> errors
    ) {}
}
