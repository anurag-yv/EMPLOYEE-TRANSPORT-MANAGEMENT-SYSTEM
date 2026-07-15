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

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(final int pNumberOfSeats) {
        this.numberOfSeats = pNumberOfSeats;
    }

    public String getPassengerDetails() {
        return passengerDetails;
    }

    public void setPassengerDetails(final String pPassengerDetails) {
        this.passengerDetails = pPassengerDetails;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long pId) {
        this.id = pId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(final Employee pEmployee) {
        this.employee = pEmployee;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(final Route pRoute) {
        this.route = pRoute;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String pStatus) {
        this.status = pStatus;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(final LocalDateTime pBookedAt) {
        this.bookedAt = pBookedAt;
    }
}
