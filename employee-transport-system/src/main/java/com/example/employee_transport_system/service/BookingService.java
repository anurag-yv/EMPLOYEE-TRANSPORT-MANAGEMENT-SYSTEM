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
import org.springframework.kafka.core.KafkaTemplate;
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
    private final KafkaTemplate<String, String> kafkaTemplate;

    public BookingService(BookingRepository bookingRepo, 
                          EmployeeRepository employeeRepo, 
                          RouteRepository routeRepo,
                          SystemConfigRepository configRepo,
                          KafkaTemplate<String, String> kafkaTemplate) {
        this.bookingRepo = bookingRepo;
        this.employeeRepo = employeeRepo;
        this.routeRepo = routeRepo;
        this.configRepo = configRepo;
        this.kafkaTemplate = kafkaTemplate;
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



        Route route = routeRepo.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a");
            LocalTime pickup = LocalTime.parse(route.getPickupTime().toUpperCase(), dtf);
            LocalTime now = LocalTime.now();
            
            if (now.plusHours(config.getBookingWindow()).isAfter(pickup) && now.isBefore(pickup)) {
                throw new RuntimeException("Booking closed. Window: " + config.getBookingWindow() + "h before departure.");
            }
        } catch (Exception e) {
            // Ignore parse errors
        }



        if (bookingRepo.existsByEmployeeAndRouteAndStatus(employee, route, "CONFIRMED")) {
            throw new DuplicateBookingException("You have already booked this route");
        }

        // If an EMPLOYEE (Rider) is trying to book, ensure only one rider is assigned
        if (!"CITIZEN".equalsIgnoreCase(employee.getRole())) {
            boolean hasRider = bookingRepo.findByRoute(route).stream()
                .anyMatch(b -> "CONFIRMED".equals(b.getStatus()) && !"CITIZEN".equalsIgnoreCase(b.getEmployee().getRole()));
            if (hasRider) {
                throw new BookingLimitExceededException("This bus already has a rider assigned.");
            }
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

        Booking saved = bookingRepo.save(booking);
        
        // Publish booking event
        try {
            String event = String.format(
                    "{\"type\":\"BOOKING_CREATED\",\"bookingId\":%d,\"employeeEmail\":\"%s\",\"routeId\":%d,\"seats\":%d}",
                    saved.getId(), email, routeId, seats);
            kafkaTemplate.send("booking-events", event);
        } catch (Exception e) {
            // Ignore Kafka errors
        }
        
        return saved;
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

    /**
     * Get the Kafka template for publishing events.
     */
    public KafkaTemplate<String, String> getKafkaTemplate() {
        return kafkaTemplate;
    }

    @Transactional
    public void cancelBookingByEmail(final String email, final Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getEmployee().getEmail().equals(email)) throw new RuntimeException("Unauthorized");
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new IllegalStateException("Booking is already cancelled");
        }
        booking.setStatus("CANCELLED");
        Route route = booking.getRoute();
        if (route != null) {
            route.setBookedSeats(Math.max(0, route.getBookedSeats() - booking.getNumberOfSeats()));
            routeRepo.save(route);
        }
        bookingRepo.save(booking);

        // Publish cancellation event
        try {
            String event = String.format(
                    "{\"type\":\"BOOKING_CANCELLED\",\"bookingId\":%d,\"employeeEmail\":\"%s\",\"routeId\":%s,\"seats\":%d}",
                    bookingId, email, route != null ? String.valueOf(route.getId()) : "null", booking.getNumberOfSeats());
            kafkaTemplate.send("booking-events", event);
        } catch (Exception e) {
            // Ignore Kafka errors
        }
    }

    @Transactional
    public void cancelBooking(final Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if ("CANCELLED".equals(booking.getStatus())) {
            return;
        }
        booking.setStatus("CANCELLED");
        Route route = booking.getRoute();
        if (route != null) {
            route.setBookedSeats(Math.max(0, route.getBookedSeats() - booking.getNumberOfSeats()));
            routeRepo.save(route);
        }
        bookingRepo.save(booking);
        
        // Publish cancellation event
        try {
            String event = String.format(
                    "{\"type\":\"BOOKING_CANCELLED\",\"bookingId\":%d,\"employeeEmail\":\"%s\",\"routeId\":%s,\"seats\":%d}",
                    bookingId, booking.getEmployee().getEmail(), route != null ? String.valueOf(route.getId()) : "null", booking.getNumberOfSeats());
            kafkaTemplate.send("booking-events", event);
        } catch (Exception e) {
            // Ignore Kafka errors
        }
    }
}
