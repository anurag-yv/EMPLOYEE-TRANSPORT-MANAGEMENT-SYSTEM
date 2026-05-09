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
        return bookingService.bookSeatByEmail(email, request.getRouteId());
    }

    @GetMapping("/my")
    public List<Booking> getMyBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return bookingService.getBookingsByEmail(email);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        bookingService.cancelBookingByEmail(email, id);
        return ResponseEntity.ok("Booking cancelled successfully");
    }
}
