package com.penny.expense.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested expense ID does not exist.
 *
 * SRP + OCP: Using domain-specific exceptions means GlobalExceptionHandler
 * can map them with precise @ExceptionHandler methods — no brittle
 * instanceof checks or message-string parsing required.
 * Adding a new domain exception adds a new handler method — it never
 * modifies existing ones (OCP at the exception-handling level).
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ExpenseNotFoundException extends RuntimeException {

    public ExpenseNotFoundException(Long id) {
        super("Expense not found: " + id);
    }
}
