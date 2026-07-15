package com.example.employee_transport_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 */
@ControllerAdvice
public final class GlobalExceptionHandler {

    /**
     * Handles bad credentials exceptions.
     * @param ex the exception
     * @return response entity with error message
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(
            final BadCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handles resource not found (404).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(
            final ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles seat unavailable / duplicate booking / booking limit (409 Conflict).
     */
    @ExceptionHandler({SeatUnavailableException.class, DuplicateBookingException.class, BookingLimitExceededException.class})
    public ResponseEntity<Map<String, String>> handleConflict(
            final RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handles runtime exceptions (400 Bad Request fallback).
     * @param ex the exception
     * @return response entity with error message
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(
            final RuntimeException ex) {
        ex.printStackTrace(); // Print to console for debugging
        Map<String, String> error = new HashMap<>();
        String msg = ex.getMessage();
        Throwable cause = ex.getCause();
        while (cause != null) {
            msg += " | Cause: " + cause.getMessage();
            cause = cause.getCause();
        }
        error.put("message", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles general exceptions.
     * @param ex the exception
     * @return response entity with error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(
            final Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "An unexpected error occurred: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
