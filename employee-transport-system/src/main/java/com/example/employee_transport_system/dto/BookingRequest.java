package com.example.employee_transport_system.dto;

import jakarta.validation.constraints.NotNull;

public class BookingRequest {

    private Long employeeId;

    @NotNull(message = "Route ID is required")
    private Long routeId;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }

}
