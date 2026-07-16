package com.example.employee_transport_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "routes", indexes = {
    @jakarta.persistence.Index(name = "idx_route_src_dest", columnList = "source,destination")
})
public final class Route extends AbstractAuditable {

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

    // Tracks occupancy for capacity management
    private int bookedSeats = 0;

    // Optimistic locking version for concurrent update protection
    @Version
    private Long version;

    private double budget;

    public Route() {
    }

    public Route(String source, String destination, int capacity, String pickupTime) {
        this.source = source;
        this.destination = destination;
        this.capacity = capacity;
        this.pickupTime = pickupTime;
        this.bookedSeats = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(String pickupTime) {
        this.pickupTime = pickupTime;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }
}
