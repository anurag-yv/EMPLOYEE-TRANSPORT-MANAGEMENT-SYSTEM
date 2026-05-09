package com.example.employee_transport_system.controller;

import com.example.employee_transport_system.entity.Alert;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.repository.AlertRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertRepository alertRepository;
    private final EmployeeRepository employeeRepository;

    public AlertController(AlertRepository alertRepository, EmployeeRepository employeeRepository) {
        this.alertRepository = alertRepository;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping
    public Alert createAlert(@RequestBody Alert alert) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        alert.setEmployee(employee);
        return alertRepository.save(alert);
    }

    @GetMapping("/active")
    public List<Alert> getActiveAlerts() {
        return alertRepository.findByResolvedFalseOrderByTimestampDesc();
    }

    @PutMapping("/{id}/resolve")
    public Alert resolveAlert(@PathVariable Long id) {
        Alert alert = alertRepository.findById(id).orElseThrow();
        alert.setResolved(true);
        return alertRepository.save(alert);
    }
}
