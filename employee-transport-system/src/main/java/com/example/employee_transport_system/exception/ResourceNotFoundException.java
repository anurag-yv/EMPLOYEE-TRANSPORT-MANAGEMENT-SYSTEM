package com.example.employee_transport_system.exception;

public class ResourceNotFoundException extends RuntimeException {


    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
