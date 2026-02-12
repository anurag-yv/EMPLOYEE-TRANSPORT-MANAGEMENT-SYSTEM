package com.example.employee_transport_system.exception;

public class ResourceNotFoundException extends RuntimeException {

    // No-arg constructor
    public ResourceNotFoundException() {
        super();
    }

    // Constructor with custom message
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
