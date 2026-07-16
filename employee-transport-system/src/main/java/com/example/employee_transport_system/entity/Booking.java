package com.example.employee_transport_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", indexes = {
    @jakarta.persistence.Index(name = "idx_booking_emp", columnList = "employee_id"),
    @jakarta.persistence.Index(name = "idx_booking_route", columnList = "route_id"),
    @jakarta.persistence.Index(name = "idx_booking_status", columnList = "status"),
    @jakarta.persistence.Index(name = "idx_booking_idem", columnList = "idempotencyKey")
})
public final class Booking extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    // Defaults to 1 seat for citizen bookings, can be modified by admin
    private int numberOfSeats = 1;

    private String passengerDetails;

    // CONFIRMED or CANCELLED
    private String status = "CONFIRMED";

    private LocalDateTime bookedAt = LocalDateTime.now();

    // Idempotency key prevents duplicate bookings on retries
    @Column(unique = true)
    private String idempotencyKey;

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public String getPassengerDetails() {
        return passengerDetails;
    }

    public void setPassengerDetails(String passengerDetails) {
        this.passengerDetails = passengerDetails;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
