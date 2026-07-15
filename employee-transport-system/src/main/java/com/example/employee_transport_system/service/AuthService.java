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

/**
 * Service for handling authentication and user registration.
 * This class is designed to be proxied by Spring and is not intended
 * for manual extension.
 */
@Service
public class AuthService {

    /** Repository for employee data. */
    private final EmployeeRepository employeeRepo;
    /** Repository for admin data. */
    private final AdminRepository adminRepo;
    /** Encoder for passwords. */
    private final PasswordEncoder passwordEncoder;
    /** Utility for JWT operations. */
    private final JwtUtil jwtUtil;

    /**
     * Authenticates a user based on email and password.
     * @param request the authentication request
     * @return the authentication response containing JWT
     */
    public AuthResponse authenticate(final AuthRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new BadCredentialsException("Email and password are required");
        }

        String email = request.getEmail().toLowerCase().trim();

        // Try Employee first
        Employee employee = employeeRepo.findByEmail(email).orElse(null);
        if (employee != null) {
            if (passwordEncoder.matches(request.getPassword(),
                    employee.getPassword())) {
                String token = jwtUtil.generateToken(employee.getEmail(),
                        employee.getRole().toUpperCase());
                return new AuthResponse(token);
            }
        }

        // Try Admin next
        Admin admin = adminRepo.findByEmail(email).orElse(null);
        if (admin != null) {
            if (passwordEncoder.matches(request.getPassword(),
                    admin.getPassword())) {
                String token = jwtUtil.generateToken(admin.getEmail(), "ADMIN");
                return new AuthResponse(token);
            }
        }

        throw new BadCredentialsException("Invalid email or password");
    }

    /**
     * Registers a new user in the system.
     * @param request the registration details
     */
    public void register(final RegisterRequest request) {
        if (request.getEmail() == null || request.getPassword() == null
                || request.getName() == null) {
            throw new RuntimeException("All fields are required.");
        }

        String email = request.getEmail().toLowerCase().trim();
        boolean existsAsAdmin = adminRepo.findByEmail(email).isPresent();
        boolean existsAsEmployee = employeeRepo.findByEmail(email).isPresent();

        if (existsAsAdmin || existsAsEmployee) {
            throw new RuntimeException("Email is already registered.");
        }

        // Public registration never creates ADMIN accounts.
        // Any "ADMIN" role sent by the client is ignored — force EMPLOYEE.
        try {
            String effectiveRole = request.getRole();
            if (effectiveRole != null && "ADMIN".equalsIgnoreCase(effectiveRole)) {
                effectiveRole = "EMPLOYEE";
            }

            if ("CITIZEN".equalsIgnoreCase(effectiveRole)) {
                Employee employee = new Employee();
                employee.setName(request.getName());
                employee.setEmail(email);
                employee.setPassword(passwordEncoder.encode(
                        request.getPassword()));
                employee.setRole("CITIZEN");
                employeeRepo.save(employee);
            } else {
                Employee employee = new Employee();
                employee.setName(request.getName());
                employee.setEmail(email);
                employee.setPassword(passwordEncoder.encode(
                        request.getPassword()));
                employee.setRole("EMPLOYEE");
                employeeRepo.save(employee);
            }
        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Creates a new admin account. Only callable by existing authenticated ADMIN users.
     * @param request the registration details for the new admin
     */
    public void createAdmin(final RegisterRequest request) {
        if (request.getEmail() == null || request.getPassword() == null
                || request.getName() == null) {
            throw new RuntimeException("All fields are required.");
        }

        String email = request.getEmail().toLowerCase().trim();
        boolean existsAsAdmin = adminRepo.findByEmail(email).isPresent();
        boolean existsAsEmployee = employeeRepo.findByEmail(email).isPresent();

        if (existsAsAdmin || existsAsEmployee) {
            throw new RuntimeException("Email is already registered.");
        }

        try {
            Admin admin = new Admin();
            admin.setEmail(email);
            admin.setName(request.getName());
            admin.setPassword(passwordEncoder.encode(request.getPassword()));
            admin.setRole("ADMIN");
            adminRepo.save(admin);
        } catch (Exception e) {
            throw new RuntimeException("Admin creation failed: " + e.getMessage());
        }
    }

    /**
     * Constructor for AuthService.
     * @param pEmployeeRepo repo for employees
     * @param pAdminRepo repo for admins
     * @param pPasswordEncoder encoder for passwords
     * @param pJwtUtil util for tokens
     */
    public AuthService(final EmployeeRepository pEmployeeRepo,
                       final AdminRepository pAdminRepo,
                       final PasswordEncoder pPasswordEncoder,
                       final JwtUtil pJwtUtil) {
        this.employeeRepo = pEmployeeRepo;
        this.adminRepo = pAdminRepo;
        this.passwordEncoder = pPasswordEncoder;
        this.jwtUtil = pJwtUtil;
    }

}
