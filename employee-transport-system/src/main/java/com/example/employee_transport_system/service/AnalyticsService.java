package com.example.employee_transport_system.service;

import com.example.employee_transport_system.repository.BookingRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import com.example.employee_transport_system.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service providing dashboard metrics and usage analytics.
 */
@Service
public class AnalyticsService {

    private final BookingRepository bookingRepo;
    private final RouteRepository routeRepo;
    private final EmployeeRepository employeeRepo;

    public AnalyticsService(final BookingRepository bookingRepo,
                            final RouteRepository routeRepo,
                            final EmployeeRepository employeeRepo) {
        this.bookingRepo = bookingRepo;
        this.routeRepo = routeRepo;
        this.employeeRepo = employeeRepo;
    }

    public Map<String, Object> getDashboardAnalytics() {
        final Map<String, Object> data = new HashMap<>();

        data.put("totalBookings", bookingRepo.count());
        data.put("activeRoutes", routeRepo.count());
        data.put("totalEmployees", employeeRepo.count());

        final Map<String, Integer> weeklyUsage = new LinkedHashMap<>();
        final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (final String day : days) {
            weeklyUsage.put(day, 0);
        }

        // Query only timestamps to avoid full entity loading overhead
        final List<LocalDateTime> bookedDates = bookingRepo.findAllBookedAtDates();
        for (final LocalDateTime dt : bookedDates) {
            final String dayName = dt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            if (weeklyUsage.containsKey(dayName)) {
                weeklyUsage.put(dayName, weeklyUsage.get(dayName) + 1);
            }
        }

        data.put("weeklyUsage", weeklyUsage);
        return data;
    }
}
