package com.example.employee_transport_system.service;

import com.example.employee_transport_system.dto.BookingDTO;
import com.example.employee_transport_system.entity.Booking;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.entity.Route;
import com.example.employee_transport_system.repository.BookingRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import com.example.employee_transport_system.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepo;
    private final EmployeeRepository employeeRepo;
    private final RouteRepository routeRepo;

    public List<Booking> getAllBookings() {
        return bookingRepo.findAll();
    }

    public Booking getBookingById(Long id) {
        return bookingRepo.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public Booking createBooking(BookingDTO dto) {
        Employee employee = employeeRepo.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Route route = routeRepo.findById(dto.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));

        Booking booking = Booking.builder()
                .employee(employee)
                .route(route)
                .build();

        return bookingRepo.save(booking);
    }

    public void deleteBooking(Long id) {
        bookingRepo.deleteById(id);
    }
}
