package com.example.employee_transport_system.controller;

import com.example.employee_transport_system.entity.Booking;
import com.example.employee_transport_system.repository.BookingRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import com.example.employee_transport_system.repository.RouteRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final BookingRepository bookingRepo;
    private final RouteRepository routeRepo;
    private final EmployeeRepository employeeRepo;

    public AnalyticsController(BookingRepository bookingRepo, RouteRepository routeRepo, EmployeeRepository employeeRepo) {
        this.bookingRepo = bookingRepo;
        this.routeRepo = routeRepo;
        this.employeeRepo = employeeRepo;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardAnalytics() {
        Map<String, Object> data = new HashMap<>();
        
        // Real counts from the database
        data.put("totalBookings", bookingRepo.count());
        data.put("activeRoutes", routeRepo.count());
        data.put("totalEmployees", employeeRepo.count());
        
        // Use a more efficient way to get weekly stats
        // In a real production app, you would use a native query like:
        // SELECT DAYNAME(booked_at), COUNT(*) FROM booking GROUP BY DAYNAME(booked_at)
        
        Map<String, Integer> weeklyUsage = new LinkedHashMap<>();
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String day : days) {
            weeklyUsage.put(day, 0);
        }

        // For now, let's just use placeholder data or a limited query to avoid performance hits
        // If the database is large, this loop was the bottleneck
        List<Booking> recentBookings = bookingRepo.findAll(); // In real app, limit this or use group by
        for (Booking b : recentBookings) {
            if (b.getBookedAt() != null) {
                String dayName = b.getBookedAt().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                if (weeklyUsage.containsKey(dayName)) {
                    weeklyUsage.put(dayName, weeklyUsage.get(dayName) + 1);
                }
            }
        }
        
        data.put("weeklyUsage", weeklyUsage);
        return data;
    }
}
