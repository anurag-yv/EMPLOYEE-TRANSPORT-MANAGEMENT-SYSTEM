package com.example.employee_transport_system.controller;

import com.example.employee_transport_system.dto.BookingRequest;
import com.example.employee_transport_system.entity.Booking;
import com.example.employee_transport_system.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/booking")
@Tag(name = "Booking Management", description = "Endpoints for employee ride bookings and scheduling")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    @Operation(summary = "Get all bookings (supports optional pagination and sorting)")
    public ResponseEntity<?> getAllBookings(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        if (page == null || size == null) {
            return ResponseEntity.ok(bookingService.getAllBookingsList());
        }
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(bookingService.getAllBookings(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking details by ID")
    public ResponseEntity<Booking> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new ride booking")
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Booking booking = bookingService.bookSeatByEmail(
                email,
                request.getRouteId(),
                request.getNumberOfSeats(),
                request.getPassengerDetails(),
                request.getIdempotencyKey()
        );
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's bookings (supports optional pagination)")
    public ResponseEntity<?> getMyBookings(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (page == null || size == null) {
            return ResponseEntity.ok(bookingService.getBookingsByEmailList(email));
        }
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(bookingService.getBookingsByEmail(email, pageable));
    }

    // Admin-only endpoint for manual rider assignment
    @PostMapping("/assign")
    @Operation(summary = "Assign a booking via admin controls")
    public ResponseEntity<Booking> assignBooking(@RequestBody java.util.Map<String, Object> payload) {
        String email = (String) payload.get("email");
        Long routeId = Long.valueOf(payload.get("routeId").toString());
        Booking booking = bookingService.bookSeatByEmail(email, routeId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/route/{routeId}")
    @Operation(summary = "Get all bookings for a specific route")
    public ResponseEntity<List<Booking>> getBookingsByRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(bookingService.getBookingsByRouteId(routeId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a booking (user authorization checked)")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        bookingService.cancelBookingByEmail(email, id);
        return ResponseEntity.ok("Booking cancelled successfully");
    }

    // Bypass authorization checks - admin override for cancellations
    @DeleteMapping("/admin/{id}")
    @Operation(summary = "Cancel a booking directly as administrator")
    public ResponseEntity<String> cancelBookingAdmin(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok("Booking cancelled by admin successfully");
    }
}