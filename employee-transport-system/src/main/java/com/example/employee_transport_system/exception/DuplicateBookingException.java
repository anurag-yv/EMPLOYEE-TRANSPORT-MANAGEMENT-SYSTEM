package com.example.employee_transport_system.exception;

/**
 * Thrown when a user tries to book a route they have already booked.
 */
public class DuplicateBookingException extends RuntimeException {
    public DuplicateBookingException(String message) {
        super(message);
    }
}