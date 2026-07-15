package com.example.employee_transport_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Entity representing a transport route.
 */
@Entity
@Table(name = "routes")
public final class Route {

    /** The unique identifier of the route. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The starting location. */
    @NotBlank(message = "From location is required")
    private String source;

    /** The destination location. */
    @NotBlank(message = "To location is required")
    private String destination;

    /** The total seat capacity of the vehicle. */
    @Min(value = 1, message = "Capacity must be at least 1")
    private int capacity;

    /** The scheduled pickup time. */
    private String pickupTime;

    /** The number of seats already booked. */
    private int bookedSeats = 0;

    /** Optimistic locking version field. */
    @Version
    private Long version;

    /** The budget or cost allocated to this trip. */
    private double budget;

    public Route() {
    }

    public Route(final String pSource, final String pDestination,
                 final int pCapacity, final String pPickupTime) {
        this.source = pSource;
        this.destination = pDestination;
        this.capacity = pCapacity;
        this.pickupTime = pPickupTime;
        this.bookedSeats = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long pId) {
        this.id = pId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String pSource) {
        this.source = pSource;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(final String pDestination) {
        this.destination = pDestination;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(final int pCapacity) {
        this.capacity = pCapacity;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(final String pPickupTime) {
        this.pickupTime = pPickupTime;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(final int pBookedSeats) {
        this.bookedSeats = pBookedSeats;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(final double pBudget) {
        this.budget = pBudget;
    }
}
