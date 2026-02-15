package com.example.employee_transport_system.controller;

import com.example.employee_transport_system.dto.BookingRequest;
import com.example.employee_transport_system.entity.Booking;
import com.example.employee_transport_system.repository.BookingRepository;
import com.example.employee_transport_system.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepo;

    public BookingController(BookingService bookingService,
                             BookingRepository bookingRepo) {
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
                .orElseThrow(() ->
                        new RuntimeException("Booking not found with id: " + id));
    }

    @PostMapping
    public Booking createBooking(@Valid @RequestBody BookingRequest request) {
        return bookingService.bookSeat(
                request.getEmployeeId(),
                request.getRouteId()
        );
    }
}
