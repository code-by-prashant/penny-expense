package com.penny.expense.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an expense request contains invalid or unprocessable data
 * that passes Bean Validation but fails business-level validation
 * (e.g. a CSV row with a missing vendor, an unparseable date format).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidExpenseException extends RuntimeException {

    public InvalidExpenseException(String message) {
        super(message);
    }

    public InvalidExpenseException(String message, Throwable cause) {
        super(message, cause);
    }
}
