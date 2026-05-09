package com.example.employee_transport_system.service;

import com.example.employee_transport_system.entity.Booking;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.entity.Route;
import com.example.employee_transport_system.repository.BookingRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import com.example.employee_transport_system.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepo;
    private final EmployeeRepository employeeRepo;
    private final RouteRepository routeRepo;

    public BookingService(BookingRepository bookingRepo, EmployeeRepository employeeRepo, RouteRepository routeRepo) {
        this.bookingRepo = bookingRepo;
        this.employeeRepo = employeeRepo;
        this.routeRepo = routeRepo;
    }

    public List<Booking> getAllBookings() {
        return bookingRepo.findAll();
    }

    @Transactional
    public Booking bookSeatByEmail(String email, Long routeId) {
        Employee employee = employeeRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + email));

        Route route = routeRepo.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found: " + routeId));

        if (bookingRepo.existsByEmployeeAndRoute(employee, route)) {
            throw new RuntimeException("You have already booked this route");
        }

        if (route.getBookedSeats() >= route.getCapacity()) {
            throw new RuntimeException("Route is full");
        }

        Booking booking = new Booking();
        booking.setEmployee(employee);
        booking.setRoute(route);
        booking.setStatus("CONFIRMED");

        route.setBookedSeats(route.getBookedSeats() + 1);
        routeRepo.save(route);

        return bookingRepo.save(booking);
    }

    public List<Booking> getBookingsByEmail(String email) {
        Employee employee = employeeRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee record missing for email: " + email));
        return bookingRepo.findByEmployee(employee);
    }

    @Transactional
    public void cancelBookingByEmail(String email, Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        if (!booking.getEmployee().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized: This booking does not belong to you");
        }

        Route route = booking.getRoute();
        if (route != null && route.getBookedSeats() > 0) {
            route.setBookedSeats(route.getBookedSeats() - 1);
            routeRepo.save(route);
        }

        bookingRepo.deleteById(bookingId);
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        Route route = booking.getRoute();
        if (route != null && route.getBookedSeats() > 0) {
            route.setBookedSeats(route.getBookedSeats() - 1);
            routeRepo.save(route);
        }
        bookingRepo.deleteById(bookingId);
    }
}
