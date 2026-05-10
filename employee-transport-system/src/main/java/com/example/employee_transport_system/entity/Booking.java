package com.example.employee_transport_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entity representing a trip booking.
 */
@Entity
@Table(name = "bookings")
public final class Booking {

    /** The unique identifier of the booking. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The employee who made the booking. */
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /** The route being booked. */
    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    /** The number of seats booked. */
    private int numberOfSeats = 1;

    /** Details for other passengers if booking multiple seats. */
    private String passengerDetails;

    /** The status of the booking (e.g., CONFIRMED). */
    private String status = "CONFIRMED";

    /** The timestamp when the booking was created. */
    private LocalDateTime bookedAt = LocalDateTime.now();

    /**
     * Gets the number of seats.
     * @return seat count
     */
    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    /**
     * Sets the number of seats.
     * @param pNumberOfSeats seat count
     */
    public void setNumberOfSeats(final int pNumberOfSeats) {
        this.numberOfSeats = pNumberOfSeats;
    }

    /**
     * Gets passenger details.
     * @return details string
     */
    public String getPassengerDetails() {
        return passengerDetails;
    }

    /**
     * Sets passenger details.
     * @param pPassengerDetails details string
     */
    public void setPassengerDetails(final String pPassengerDetails) {
        this.passengerDetails = pPassengerDetails;
    }


    /**
     * Gets the booking ID.
     * @return the ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the booking ID.
     * @param pId the ID
     */
    public void setId(final Long pId) {
        this.id = pId;
    }

    /**
     * Gets the employee.
     * @return the employee
     */
    public Employee getEmployee() {
        return employee;
    }

    /**
     * Sets the employee.
     * @param pEmployee the employee
     */
    public void setEmployee(final Employee pEmployee) {
        this.employee = pEmployee;
    }

    /**
     * Gets the route.
     * @return the route
     */
    public Route getRoute() {
        return route;
    }

    /**
     * Sets the route.
     * @param pRoute the route
     */
    public void setRoute(final Route pRoute) {
        this.route = pRoute;
    }

    /**
     * Gets the status.
     * @return status string
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status.
     * @param pStatus status string
     */
    public void setStatus(final String pStatus) {
        this.status = pStatus;
    }

    /**
     * Gets the booking time.
     * @return timestamp
     */
    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    /**
     * Sets the booking time.
     * @param pBookedAt timestamp
     */
    public void setBookedAt(final LocalDateTime pBookedAt) {
        this.bookedAt = pBookedAt;
    }
}
