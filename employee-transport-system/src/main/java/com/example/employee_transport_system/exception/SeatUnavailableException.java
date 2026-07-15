package com.example.employee_transport_system.exception;

/**
 * Thrown when there are not enough available seats on a route.
 */
public class SeatUnavailableException extends RuntimeException {
    public SeatUnavailableException(String message) {
        super(message);
    }
}