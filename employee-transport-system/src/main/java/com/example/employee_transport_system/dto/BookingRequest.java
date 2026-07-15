package com.example.employee_transport_system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for booking requests.
 */
public class BookingRequest {

    private Long employeeId;

    @NotNull(message = "Route ID is required")
    private Long routeId;

    @Min(value = 1, message = "At least one seat must be booked")
    private int numberOfSeats = 1;

    @jakarta.validation.constraints.NotBlank(message = "Description / Passenger Details is required")
    private String passengerDetails;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public int getNumberOfSeats() { return numberOfSeats; }
    public void setNumberOfSeats(int numberOfSeats) { this.numberOfSeats = numberOfSeats; }

    public String getPassengerDetails() { return passengerDetails; }
    public void setPassengerDetails(String passengerDetails) { this.passengerDetails = passengerDetails; }
}
