package com.example.employee_transport_system.dto;

import lombok.Data;

@Data
public class RouteDTO {
    private String origin;
    private String destination;
    private int capacity;
}
