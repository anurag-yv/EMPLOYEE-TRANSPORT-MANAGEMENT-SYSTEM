package com.example.employee_transport_system.service;

import com.example.employee_transport_system.entity.Booking;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.entity.Route;
import com.example.employee_transport_system.entity.SystemConfig;
import com.example.employee_transport_system.exception.BookingLimitExceededException;
import com.example.employee_transport_system.exception.ResourceNotFoundException;
import com.example.employee_transport_system.repository.BookingRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import com.example.employee_transport_system.repository.RouteRepository;
import com.example.employee_transport_system.repository.SystemConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepo;

    @Mock
    private EmployeeRepository employeeRepo;

    @Mock
    private RouteRepository routeRepo;

    @Mock
    private SystemConfigRepository configRepo;

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBookSeatByEmail_Success() {
        String email = "test@example.com";
        Long routeId = 1L;
        
        Employee employee = new Employee();
        employee.setEmail(email);
        
        Route route = new Route();
        route.setId(routeId);
        route.setCapacity(10);
        route.setBookedSeats(2);
        route.setPickupTime("10:00 AM");

        SystemConfig config = new SystemConfig();
        config.setMaxBookings(5);
        config.setBookingWindow(2);

        when(configRepo.findById("global")).thenReturn(Optional.of(config));
        when(employeeRepo.findByEmail(email)).thenReturn(Optional.of(employee));
        when(bookingRepo.findByEmployeeAndStatus(employee, "CONFIRMED")).thenReturn(new ArrayList<>());
        when(routeRepo.findById(routeId)).thenReturn(Optional.of(route));
        when(bookingRepo.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.bookSeatByEmail(email, routeId);

        assertNotNull(result);
        assertEquals("CONFIRMED", result.getStatus());
        assertEquals(3, route.getBookedSeats());
        verify(bookingRepo, times(1)).save(any(Booking.class));
    }

    @Test
    void testBookSeatByEmail_UserNotFound() {
        String email = "missing@example.com";
        when(employeeRepo.findByEmail(email)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> 
            bookingService.bookSeatByEmail(email, 1L)
        );

        assertEquals("User not found", exception.getMessage());
    }


}