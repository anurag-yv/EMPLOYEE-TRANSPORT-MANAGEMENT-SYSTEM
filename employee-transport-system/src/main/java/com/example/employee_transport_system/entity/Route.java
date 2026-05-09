package com.example.employee_transport_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "From location is required")
    private String source;

    @NotBlank(message = "To location is required")
    private String destination;

    @Min(value = 1, message = "Capacity must be at least 1")
    private int capacity;

    private String pickupTime;

    private int bookedSeats = 0;

    // Default constructor
    public Route() {}

    // Constructor without ID
    public Route(String source, String destination, int capacity, String pickupTime) {
        this.source = source;
        this.destination = destination;
        this.capacity = capacity;
        this.pickupTime = pickupTime;
        this.bookedSeats = 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getPickupTime() { return pickupTime; }
    public void setPickupTime(String pickupTime) { this.pickupTime = pickupTime; }

    public int getBookedSeats() { return bookedSeats; }
    public void setBookedSeats(int bookedSeats) { this.bookedSeats = bookedSeats; }
}
