package com.finance.personalfinancemanager.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle JSON parsing errors (like invalid date formats)
     * Return 400 Bad Request instead of 500 Internal Server Error
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonParseException(HttpMessageNotReadableException ex) {
        String errorMessage = "Invalid request format";

        // Extract more specific error message if available
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            String fieldName = ife.getPath().isEmpty() ? "field" : ife.getPath().get(0).getFieldName();
            errorMessage = String.format("Invalid format for '%s': %s", fieldName, ife.getValue());
        } else if (ex.getMessage() != null && ex.getMessage().contains("LocalDate")) {
            errorMessage = "Invalid date format. Expected format: YYYY-MM-DD";
        }

        return ResponseEntity.badRequest()
                .body(Map.of("error", errorMessage));
    }
}