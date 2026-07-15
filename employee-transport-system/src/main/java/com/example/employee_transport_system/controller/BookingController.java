package com.example.employee_transport_system.controller;

import com.example.employee_transport_system.dto.BookingRequest;
import com.example.employee_transport_system.entity.Booking;
import com.example.employee_transport_system.repository.BookingRepository;
import com.example.employee_transport_system.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepo;

    public BookingController(BookingService bookingService, BookingRepository bookingRepo) {
        this.bookingService = bookingService;
        this.bookingRepo = bookingRepo;
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepo.findAll();
    }

    @GetMapping("/{id}")
    public Booking getBooking(@PathVariable Long id) {
        return bookingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    @PostMapping
    public Booking createBooking(@Valid @RequestBody BookingRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return bookingService.bookSeatByEmail(
            email, 
            request.getRouteId(), 
            request.getNumberOfSeats(), 
            request.getPassengerDetails()
        );
    }


    @GetMapping("/my")
    public List<Booking> getMyBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return bookingService.getBookingsByEmail(email);
    }

    @PostMapping("/assign")
    public Booking assignBooking(@RequestBody java.util.Map<String, Object> payload) {
        String email = (String) payload.get("email");
        Long routeId = Long.valueOf(payload.get("routeId").toString());
        return bookingService.bookSeatByEmail(email, routeId);
    }

    @GetMapping("/route/{routeId}")
    public List<Booking> getBookingsByRoute(@PathVariable Long routeId) {
        return bookingRepo.findAll().stream()
                .filter(b -> b.getRoute() != null && b.getRoute().getId().equals(routeId))
                .toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        bookingService.cancelBookingByEmail(email, id);
        return ResponseEntity.ok("Booking cancelled successfully");
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<String> cancelBookingAdmin(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok("Booking cancelled by admin successfully");
    }
}
