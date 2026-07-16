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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(BookingService.class);

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

    // Accepts multiple pickup time formats to handle legacy data and client inconsistencies
    private java.time.LocalTime parseLocalTime(String timeStr) {
        if (timeStr == null) {
            return null;
        }
        String cleaned = timeStr.trim().toUpperCase();
        String[] patterns = {"hh:mm a", "h:mm a", "HH:mm", "H:mm"};
        for (String pattern : patterns) {
            try {
                return java.time.LocalTime.parse(cleaned,
                        java.time.format.DateTimeFormatter.ofPattern(pattern, java.util.Locale.ENGLISH));
            } catch (Exception e) {
                // Try next pattern
            }
        }
        return null;
    }

    public Booking bookSeatByEmail(String email, Long routeId,
                                 int seats, String details) {
        return bookSeatByEmail(email, routeId, seats, details, null);
    }

    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "bookingService")
    @io.github.resilience4j.retry.annotation.Retry(name = "bookingService")
    public Booking bookSeatByEmail(String email, Long routeId,
                                 int seats, String details, String idempotencyKey) {
        // Optimistic locking retry - avoids distributed locks under high concurrency
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                return executeBookSeatByEmail(email, routeId, seats, details, idempotencyKey);
            } catch (org.springframework.orm.ObjectOptimisticLockingFailureException
                    | jakarta.persistence.OptimisticLockException e) {
                if (i == maxRetries - 1) {
                    throw new SeatUnavailableException("Seat reservation failed due to concurrency. Try again.");
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new SeatUnavailableException("Seat reservation failed. Please try again.");
    }

    @Transactional
    public Booking executeBookSeatByEmail(String email, Long routeId,
                                          int seats, String details, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            java.util.Optional<Booking> existing = bookingRepo.findByIdempotencyKey(idempotencyKey.trim());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        SystemConfig config = getConfig();
        Employee employee = employeeRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Route route = routeRepo.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        // Check if booking window has closed
        java.time.LocalTime pickup = parseLocalTime(route.getPickupTime());
        if (pickup != null) {
            java.time.LocalTime now = java.time.LocalTime.now();
            if (now.plusHours(config.getBookingWindow()).isAfter(pickup) && now.isBefore(pickup)) {
                throw new IllegalStateException("Booking closed. Window: "
                        + config.getBookingWindow() + "h before departure.");
            }
        }

        if (bookingRepo.existsByEmployeeAndRouteAndStatus(employee, route, "CONFIRMED")) {
            throw new DuplicateBookingException("You have already booked this route");
        }

        // Non-citizens (riders) can only share a route with one rider at most
        if (!"CITIZEN".equalsIgnoreCase(employee.getRole())) {
            boolean hasRider = bookingRepo.findByRoute(route).stream()
                .anyMatch(b -> "CONFIRMED".equals(b.getStatus())
                        && !"CITIZEN".equalsIgnoreCase(b.getEmployee().getRole()));
            if (hasRider) {
                throw new BookingLimitExceededException("This bus already has a rider assigned.");
            }
        }

        if (route.getBookedSeats() + seats > route.getCapacity()) {
            throw new SeatUnavailableException("Not enough seats. Available: "
                    + (route.getCapacity() - route.getBookedSeats()));
        }

        Booking booking = new Booking();
        booking.setEmployee(employee);
        booking.setRoute(route);
        booking.setNumberOfSeats(seats);
        booking.setPassengerDetails(details);
        booking.setStatus("CONFIRMED");
        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            booking.setIdempotencyKey(idempotencyKey.trim());
        }

        route.setBookedSeats(route.getBookedSeats() + seats);
        routeRepo.save(route);
        Booking saved = bookingRepo.save(booking);

        // Fire-and-forget Kafka event for downstream processing
        try {
            String event = String.format(
                    "{\"type\":\"BOOKING_CREATED\",\"bookingId\":%d,\"employeeEmail\":\"%s\","
                    + "\"routeId\":%d,\"seats\":%d}",
                    saved.getId(), email, routeId, seats);
            kafkaTemplate.send("booking-events", event).whenComplete((res, ex) -> {
                if (ex != null) {
                    LOGGER.error("Failed to publish booking created event for booking id {}", saved.getId(), ex);
                } else {
                    LOGGER.info("Published booking created event for booking id {}", saved.getId());
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error initiating Kafka publish for booking id {}", saved.getId(), e);
        }

        return saved;
    }

    @Transactional
    public Booking bookSeatByEmail(String email, Long routeId) {
        return bookSeatByEmail(email, routeId, 1, null, null);
    }

    public org.springframework.data.domain.Page<Booking> getAllBookings(
            org.springframework.data.domain.Pageable pageable) {
        return bookingRepo.findAll(pageable);
    }

    public List<Booking> getAllBookingsList() {
        return bookingRepo.findAll();
    }

    public org.springframework.data.domain.Page<Booking> getBookingsByEmail(
            String email, org.springframework.data.domain.Pageable pageable) {
        Employee employee = employeeRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User missing"));
        return bookingRepo.findByEmployee(employee, pageable);
    }

    public List<Booking> getBookingsByEmailList(String email) {
        Employee employee = employeeRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User missing"));
        return bookingRepo.findByEmployee(employee);
    }

    public Booking getBookingById(Long id) {
        return bookingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    public List<Booking> getBookingsByRouteId(Long routeId) {
        return bookingRepo.findByRouteId(routeId);
    }

    public KafkaTemplate<String, String> getKafkaTemplate() {
        return kafkaTemplate;
    }

    @Transactional
    public void cancelBookingByEmail(String email, Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getEmployee().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }
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

        try {
            String event = String.format(
                    "{\"type\":\"BOOKING_CANCELLED\",\"bookingId\":%d,\"employeeEmail\":\"%s\","
                    + "\"routeId\":%s,\"seats\":%d}",
                    bookingId, email, route != null ? String.valueOf(route.getId()) : "null",
                    booking.getNumberOfSeats());
            kafkaTemplate.send("booking-events", event).whenComplete((res, ex) -> {
                if (ex != null) {
                    LOGGER.error("Failed to publish booking cancelled event for booking id {}", bookingId, ex);
                } else {
                    LOGGER.info("Published booking cancelled event for booking id {}", bookingId);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error initiating Kafka publish for cancelled booking id {}", bookingId, e);
        }
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
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

        try {
            String event = String.format(
                    "{\"type\":\"BOOKING_CANCELLED\",\"bookingId\":%d,\"employeeEmail\":\"%s\","
                    + "\"routeId\":%s,\"seats\":%d}",
                    bookingId, booking.getEmployee().getEmail(),
                    route != null ? String.valueOf(route.getId()) : "null", booking.getNumberOfSeats());
            kafkaTemplate.send("booking-events", event).whenComplete((res, ex) -> {
                if (ex != null) {
                    LOGGER.error("Failed to publish booking cancelled event for booking id {}", bookingId, ex);
                } else {
                    LOGGER.info("Published booking cancelled event for booking id {}", bookingId);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error initiating Kafka publish for cancelled booking id {}", bookingId, e);
        }
    }
}