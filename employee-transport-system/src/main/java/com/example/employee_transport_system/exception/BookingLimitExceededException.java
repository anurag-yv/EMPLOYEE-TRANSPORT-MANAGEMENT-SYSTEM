package com.example.employee_transport_system.exception;

/**
 * Thrown when a user has reached their maximum number of active bookings.
 */
public class BookingLimitExceededException extends RuntimeException {
    public BookingLimitExceededException(String message) {
        super(message);
    }
}