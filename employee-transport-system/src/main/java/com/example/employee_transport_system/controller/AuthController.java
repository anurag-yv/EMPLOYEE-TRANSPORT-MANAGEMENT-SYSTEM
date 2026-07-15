package com.example.employee_transport_system.controller;

import com.example.employee_transport_system.dto.AuthRequest;
import com.example.employee_transport_system.dto.AuthResponse;
import com.example.employee_transport_system.dto.RegisterRequest;
import com.example.employee_transport_system.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/admin/create-admin")
    public ResponseEntity<String> createAdmin(@RequestBody RegisterRequest request) {
        authService.createAdmin(request);
        return ResponseEntity.ok("Admin created successfully");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Backend is UP");
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

}
