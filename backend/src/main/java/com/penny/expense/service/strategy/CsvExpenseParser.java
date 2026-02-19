package com.penny.expense.service.strategy;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.penny.expense.model.Expense;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV implementation of {@link ExpenseFileParser}.
 *
 * SRP: This class has exactly one responsibility — read a CSV file and
 * produce a list of Expense entities plus any row-level errors.
 * Previously this logic lived inside ExpenseService.uploadCsv(), giving
 * that method multiple reasons to change (CSV format, date formats,
 * column aliases, validation rules). Extracting it here isolates those
 * concerns completely.
 *
 * OCP: A new parser (Excel, JSON bank export, OFX) simply implements
 * ExpenseFileParser — ExpenseService.uploadFile() needs zero changes.
 *
 * Date parsing and column-alias resolution are private helpers scoped
 * to this class — they have no business being in a service class.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CsvExpenseParser implements ExpenseFileParser {

    private final CategorizationStrategy categorizationStrategy;

    // Supported date formats — most-specific first
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,           // 2026-01-15
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy")
    );

    // Accepted column name aliases per logical field
    private static final String[] VENDOR_ALIASES = {"vendor_name", "vendor", "merchant"};
    private static final String[] AMOUNT_ALIASES  = {"amount", "amt", "price"};
    private static final String[] DATE_ALIASES    = {"date", "expense_date", "txn_date"};
    private static final String[] DESC_ALIASES    = {"description", "desc", "notes"};

    @Override
    public ParseResult parse(MultipartFile file) {
        List<Expense> expenses = new ArrayList<>();
        List<String>  errors   = new ArrayList<>();

        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)).build()) {

            String[] headers = reader.readNext();
            if (headers == null) {
                errors.add("CSV file is empty or has no headers");
                return new ParseResult(expenses, errors);
            }

            Map<String, Integer> columnIndex = buildColumnIndex(headers);
            processRows(reader, columnIndex, expenses, errors);

        } catch (Exception e) {
            log.error("Fatal CSV parse error", e);
            errors.add("File could not be read: " + e.getMessage());
        }

        return new ParseResult(expenses, errors);
    }

    private void processRows(CSVReader reader,
                             Map<String, Integer> columnIndex,
                             List<Expense> expenses,
                             List<String> errors) throws Exception {
        String[] row;
        int rowNumber = 1;
        while ((row = reader.readNext()) != null) {
            rowNumber++;
            try {
                expenses.add(parseRow(row, columnIndex));
            } catch (IllegalArgumentException e) {
                errors.add("Row " + rowNumber + ": " + e.getMessage());
            }
        }
    }

    private Expense parseRow(String[] row, Map<String, Integer> columnIndex) {
        String vendorName = getColumn(row, columnIndex, VENDOR_ALIASES);
        String amountStr  = getColumn(row, columnIndex, AMOUNT_ALIASES);
        String dateStr    = getColumn(row, columnIndex, DATE_ALIASES);
        String description = getColumn(row, columnIndex, DESC_ALIASES);

        validateVendor(vendorName);
        BigDecimal amount = parseAmount(amountStr);
        LocalDate  date   = parseDate(dateStr);
        String     category = categorizationStrategy.categorize(vendorName);

        return Expense.builder()
                .date(date)
                .amount(amount)
                .vendorName(vendorName.trim())
                .description(description.trim())
                .category(category)
                .build();
    }

    private void validateVendor(String vendorName) {
        if (vendorName == null || vendorName.isBlank()) {
            throw new IllegalArgumentException("vendor_name is required");
        }
    }

    private BigDecimal parseAmount(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("amount is required");
        }
        try {
            BigDecimal amount = new BigDecimal(
                    raw.replace(",", "").replace("₹", "").replace("$", "").trim()
            );
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("amount must be greater than 0");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid amount value: '" + raw + "'");
        }
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return LocalDate.now();
        }
        String trimmed = raw.trim();
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (Exception ignored) {
                // try next format
            }
        }
        throw new IllegalArgumentException("unrecognised date format: '" + trimmed + "'");
    }

    private Map<String, Integer> buildColumnIndex(String[] headers) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            index.put(headers[i].trim().toLowerCase().replace(" ", "_"), i);
        }
        return index;
    }

    private String getColumn(String[] row, Map<String, Integer> index, String... aliases) {
        for (String alias : aliases) {
            Integer colIdx = index.get(alias);
            if (colIdx != null && colIdx < row.length) {
                return row[colIdx].trim();
            }
        }
        return "";
    }
}
