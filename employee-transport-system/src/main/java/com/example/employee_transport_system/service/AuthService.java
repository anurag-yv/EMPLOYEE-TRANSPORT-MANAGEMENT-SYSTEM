package com.example.employee_transport_system.service;

import com.example.employee_transport_system.config.JwtUtil;
import com.example.employee_transport_system.dto.AuthRequest;
import com.example.employee_transport_system.dto.AuthResponse;
import com.example.employee_transport_system.dto.RegisterRequest;
import com.example.employee_transport_system.entity.Admin;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.repository.AdminRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final EmployeeRepository employeeRepo;
    private final AdminRepository adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse authenticate(AuthRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new BadCredentialsException("Email and password are required");
        }
        
        String email = request.getEmail().toLowerCase().trim();
        System.out.println("Login attempt for: " + email);

        // Try Employee first
        Employee employee = employeeRepo.findByEmail(email).orElse(null);
        if (employee != null) {
            System.out.println("User found in Employee table. Verifying...");
            if (passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
                System.out.println("Login success as EMPLOYEE: " + email);
                String token = jwtUtil.generateToken(employee.getEmail(), "EMPLOYEE");
                return new AuthResponse(token);
            }
        }

        // Try Admin next
        Admin admin = adminRepo.findByEmail(email).orElse(null);
        if (admin != null) {
            System.out.println("User found in Admin table. Verifying...");
            if (passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
                System.out.println("Login success as ADMIN: " + email);
                String token = jwtUtil.generateToken(admin.getEmail(), "ADMIN");
                return new AuthResponse(token);
            }
        }

        System.out.println("Authentication failed for: " + email);
        throw new BadCredentialsException("Invalid email or password");
    }

    public void register(RegisterRequest request) {
        if (request.getEmail() == null || request.getPassword() == null || request.getName() == null) {
            throw new RuntimeException("All fields (Name, Email, Password) are required.");
        }

        String email = request.getEmail().toLowerCase().trim();
        
        // GLOBAL UNIQUE CHECK
        boolean existsAsAdmin = adminRepo.findByEmail(email).isPresent();
        boolean existsAsEmployee = employeeRepo.findByEmail(email).isPresent();

        if (existsAsAdmin || existsAsEmployee) {
            System.out.println("Registration blocked: Email " + email + " is already taken.");
            throw new RuntimeException("Registration failed: This email is already registered. Please use a different email or sign in.");
        }

        try {
            if ("ADMIN".equalsIgnoreCase(request.getRole())) {
                Admin admin = new Admin();
                admin.setEmail(email);
                admin.setName(request.getName());
                admin.setPassword(passwordEncoder.encode(request.getPassword()));
                admin.setRole("ADMIN");
                adminRepo.save(admin);
                System.out.println("Registered new ADMIN: " + email);
            } else {
                Employee employee = new Employee();
                employee.setName(request.getName());
                employee.setEmail(email);
                employee.setPassword(passwordEncoder.encode(request.getPassword()));
                employee.setRole("EMPLOYEE");
                employeeRepo.save(employee);
                System.out.println("Registered new EMPLOYEE: " + email);
            }
        } catch (Exception e) {
            System.err.println("Database error during registration: " + e.getMessage());
            throw new RuntimeException("Registration failed due to a database error. Please try again later.");
        }
    }

    public AuthService(EmployeeRepository employeeRepo, AdminRepository adminRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.employeeRepo = employeeRepo;
        this.adminRepo = adminRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

}
