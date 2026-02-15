package com.example.employee_transport_system.service;

import com.example.employee_transport_system.entity.Booking;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.entity.Route;
import com.example.employee_transport_system.repository.BookingRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import com.example.employee_transport_system.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Booking bookSeat(Long employeeId, Long routeId) {

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        Route route = routeRepo.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found with id: " + routeId));

        if (bookingRepo.existsByEmployeeAndRoute(employee, route)) {
            throw new RuntimeException("Employee already booked this route");
        }

        int bookedSeats = bookingRepo.countByRoute(route);
        if (bookedSeats >= route.getCapacity()) {
            throw new RuntimeException("Route is full");
        }

        Booking booking = new Booking();
        booking.setEmployee(employee);
        booking.setRoute(route);

        return bookingRepo.save(booking);
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        if (!bookingRepo.existsById(bookingId)) {
            throw new RuntimeException("Booking not found with id: " + bookingId);
        }
        bookingRepo.deleteById(bookingId);
    }
}
