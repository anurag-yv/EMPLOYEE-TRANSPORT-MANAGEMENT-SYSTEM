package com.example.employee_transport_system.service;

import com.example.employee_transport_system.entity.Booking;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.entity.Route;
import com.example.employee_transport_system.entity.SystemConfig;
import com.example.employee_transport_system.exception.BookingLimitExceededException;
import com.example.employee_transport_system.exception.DuplicateBookingException;
import com.example.employee_transport_system.exception.ResourceNotFoundException;
import com.example.employee_transport_system.exception.SeatUnavailableException;
import com.example.employee_transport_system.repository.BookingRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import com.example.employee_transport_system.repository.RouteRepository;
import com.example.employee_transport_system.repository.SystemConfigRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for managing bookings and reservations.
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepo;
    private final EmployeeRepository employeeRepo;
    private final RouteRepository routeRepo;
    private final SystemConfigRepository configRepo;

    public BookingService(BookingRepository bookingRepo, 
                          EmployeeRepository employeeRepo, 
                          RouteRepository routeRepo,
                          SystemConfigRepository configRepo) {
        this.bookingRepo = bookingRepo;
        this.employeeRepo = employeeRepo;
        this.routeRepo = routeRepo;
        this.configRepo = configRepo;
    }

    private SystemConfig getConfig() {
        return configRepo.findById("global").orElseGet(SystemConfig::new);
    }

    @Transactional
    public Booking bookSeatByEmail(final String email, final Long routeId, 
                                   final int seats, final String details) {
        SystemConfig config = getConfig();
        Employee employee = employeeRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 1. Max Bookings Check
        List<Booking> activeBookings = bookingRepo.findByEmployee(employee);
        if (activeBookings.size() >= config.getMaxBookings()) {
            throw new BookingLimitExceededException("Maximum booking limit reached (" + config.getMaxBookings() + ")");
        }

        Route route = routeRepo.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        // 2. Booking Window Check
        try {
            // Simple check: assume route.pickupTime is in "HH:mm AM/PM"
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a");
            LocalTime pickup = LocalTime.parse(route.getPickupTime().toUpperCase(), dtf);
            LocalTime now = LocalTime.now();
            
            // If now is within X hours of pickup, block (Simplified logic)
            if (now.plusHours(config.getBookingWindow()).isAfter(pickup) && now.isBefore(pickup)) {
                throw new RuntimeException("Booking closed. Window: " + config.getBookingWindow() + "h before departure.");
            }
        } catch (Exception e) {
            // If time format is weird, skip check or handle
        }

        if (bookingRepo.existsByEmployeeAndRoute(employee, route)) {
            throw new DuplicateBookingException("You have already booked this route");
        }

        if (route.getBookedSeats() + seats > route.getCapacity()) {
            throw new SeatUnavailableException("Not enough seats. Available: " + (route.getCapacity() - route.getBookedSeats()));
        }

        Booking booking = new Booking();
        booking.setEmployee(employee);
        booking.setRoute(route);
        booking.setNumberOfSeats(seats);
        booking.setPassengerDetails(details);
        booking.setStatus("CONFIRMED");

        route.setBookedSeats(route.getBookedSeats() + seats);
        try {
            routeRepo.save(route);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new SeatUnavailableException("Seat no longer available, please try again");
        }

        return bookingRepo.save(booking);
    }

    @Transactional
    public Booking bookSeatByEmail(final String email, final Long routeId) {
        return bookSeatByEmail(email, routeId, 1, null);
    }

    public List<Booking> getAllBookings() { return bookingRepo.findAll(); }
    public List<Booking> getBookingsByEmail(final String email) {
        Employee employee = employeeRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User missing"));
        return bookingRepo.findByEmployee(employee);
    }

    @Transactional
    public void cancelBookingByEmail(final String email, final Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getEmployee().getEmail().equals(email)) throw new RuntimeException("Unauthorized");
        Route route = booking.getRoute();
        if (route != null) {
            route.setBookedSeats(Math.max(0, route.getBookedSeats() - booking.getNumberOfSeats()));
            routeRepo.save(route);
        }
        bookingRepo.deleteById(bookingId);
    }

    @Transactional
    public void cancelBooking(final Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        Route route = booking.getRoute();
        if (route != null) {
            route.setBookedSeats(Math.max(0, route.getBookedSeats() - booking.getNumberOfSeats()));
            routeRepo.save(route);
        }
        bookingRepo.deleteById(bookingId);
    }
}
