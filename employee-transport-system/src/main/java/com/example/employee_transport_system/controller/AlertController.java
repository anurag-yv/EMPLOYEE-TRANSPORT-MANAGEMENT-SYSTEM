package com.example.employee_transport_system.controller;

import com.example.employee_transport_system.entity.Alert;
import com.example.employee_transport_system.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alerts Management", description = "Endpoints for user notifications and safety alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    @Operation(summary = "Create/raise a new alert")
    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Alert saved = alertService.createAlert(alert, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/active")
    @Operation(summary = "Get all unresolved alerts (supports optional pagination)")
    public ResponseEntity<?> getActiveAlerts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        if (page == null || size == null) {
            return ResponseEntity.ok(alertService.getActiveAlerts());
        }
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(alertService.getActiveAlerts(pageable));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's alerts (supports optional pagination)")
    public ResponseEntity<?> getMyAlerts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (page == null || size == null) {
            return ResponseEntity.ok(alertService.getMyAlerts(email));
        }
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(alertService.getMyAlerts(email, pageable));
    }

    // Admin responds with a message and marks the alert as resolved
    @PutMapping("/{id}/respond")
    @Operation(summary = "Admin responds to a specific alert")
    public ResponseEntity<Alert> respondToAlert(@PathVariable Long id,
                                                 @RequestBody java.util.Map<String, String> payload) {
        return ResponseEntity.ok(alertService.respondToAlert(id, payload.get("response")));
    }

    // Quick resolve without custom response
    @PutMapping("/{id}/resolve")
    @Operation(summary = "Admin marks alert resolved directly")
    public ResponseEntity<Alert> resolveAlert(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.resolveAlert(id));
    }
}