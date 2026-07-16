package com.example.employee_transport_system.service;

import com.example.employee_transport_system.entity.Alert;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.exception.ResourceNotFoundException;
import com.example.employee_transport_system.repository.AlertRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final EmployeeRepository employeeRepository;

    public AlertService(AlertRepository alertRepository, EmployeeRepository employeeRepository) {
        this.alertRepository = alertRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public Alert createAlert(Alert alert, String email) {
        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        alert.setEmployee(employee);
        return alertRepository.save(alert);
    }

    public List<Alert> getActiveAlerts() {
        return alertRepository.findByResolvedFalseOrderByTimestampDesc();
    }

    public Page<Alert> getActiveAlerts(Pageable pageable) {
        return alertRepository.findByResolvedFalseOrderByTimestampDesc(pageable);
    }

    public List<Alert> getMyAlerts(String email) {
        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        if (employee == null) {
            return Collections.emptyList();
        }
        return alertRepository.findByEmployeeOrderByTimestampDesc(employee);
    }

    public Page<Alert> getMyAlerts(String email, Pageable pageable) {
        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        if (employee == null) {
            return Page.empty();
        }
        return alertRepository.findByEmployeeOrderByTimestampDesc(employee, pageable);
    }

    // Mark alert as resolved with admin response
    @Transactional
    public Alert respondToAlert(Long id, String response) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        alert.setAdminResponse(response);
        alert.setRespondedAt(LocalDateTime.now());
        alert.setResolved(true);
        return alertRepository.save(alert);
    }

    // Resolve without custom response
    @Transactional
    public Alert resolveAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        alert.setResolved(true);
        alert.setRespondedAt(LocalDateTime.now());
        if (alert.getAdminResponse() == null) {
            alert.setAdminResponse("Resolved by Administrator");
        }
        return alertRepository.save(alert);
    }
}