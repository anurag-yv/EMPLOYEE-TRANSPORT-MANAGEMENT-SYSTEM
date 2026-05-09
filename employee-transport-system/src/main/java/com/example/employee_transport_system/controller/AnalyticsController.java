package com.example.employee_transport_system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardAnalytics() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalBookings", 125);
        data.put("activeRoutes", 12);
        data.put("totalEmployees", 340);
        
        // Mock data for charts
        Map<String, Integer> weeklyUsage = new HashMap<>();
        weeklyUsage.put("Mon", 20);
        weeklyUsage.put("Tue", 35);
        weeklyUsage.put("Wed", 40);
        weeklyUsage.put("Thu", 30);
        weeklyUsage.put("Fri", 50);
        
        data.put("weeklyUsage", weeklyUsage);
        return data;
    }
}
