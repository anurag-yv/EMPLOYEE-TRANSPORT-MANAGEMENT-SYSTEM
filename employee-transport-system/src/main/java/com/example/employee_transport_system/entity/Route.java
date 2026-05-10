package com.example.employee_transport_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    /** The budget or cost allocated to this trip. */
    private double budget;

    /**
     * Default constructor for JPA.
     */
    public Route() {
    }

    /**
     * Constructor without ID for creating new routes.
     * @param pSource starting location
     * @param pDestination target location
     * @param pCapacity vehicle capacity
     * @param pPickupTime scheduled time
     */
    public Route(final String pSource, final String pDestination,
                 final int pCapacity, final String pPickupTime) {
        this.source = pSource;
        this.destination = pDestination;
        this.capacity = pCapacity;
        this.pickupTime = pPickupTime;
        this.bookedSeats = 0;
    }

    /**
     * Gets the route ID.
     * @return the ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the route ID.
     * @param pId unique ID
     */
    public void setId(final Long pId) {
        this.id = pId;
    }

    /**
     * Gets the source location.
     * @return starting point
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the source location.
     * @param pSource starting point
     */
    public void setSource(final String pSource) {
        this.source = pSource;
    }

    /**
     * Gets the destination.
     * @return destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Sets the destination.
     * @param pDestination destination
     */
    public void setDestination(final String pDestination) {
        this.destination = pDestination;
    }

    /**
     * Gets the capacity.
     * @return capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Sets the capacity.
     * @param pCapacity vehicle capacity
     */
    public void setCapacity(final int pCapacity) {
        this.capacity = pCapacity;
    }

    /**
     * Gets the pickup time.
     * @return time
     */
    public String getPickupTime() {
        return pickupTime;
    }

    /**
     * Sets the pickup time.
     * @param pPickupTime time
     */
    public void setPickupTime(final String pPickupTime) {
        this.pickupTime = pPickupTime;
    }

    /**
     * Gets the number of booked seats.
     * @return count
     */
    public int getBookedSeats() {
        return bookedSeats;
    }

    /**
     * Sets the number of booked seats.
     * @param pBookedSeats count
     */
    public void setBookedSeats(final int pBookedSeats) {
        this.bookedSeats = pBookedSeats;
    }

    /**
     * Gets the budget.
     * @return cost
     */
    public double getBudget() {
        return budget;
    }

    /**
     * Sets the budget.
     * @param pBudget cost
     */
    public void setBudget(final double pBudget) {
        this.budget = pBudget;
    }
}
