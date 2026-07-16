package com.example.employee_transport_system.service;

import com.example.employee_transport_system.config.JwtUtil;
import com.example.employee_transport_system.dto.AuthRequest;
import com.example.employee_transport_system.dto.AuthResponse;
import com.example.employee_transport_system.dto.RegisterRequest;
import com.example.employee_transport_system.entity.Admin;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.entity.RefreshToken;
import com.example.employee_transport_system.repository.AdminRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import com.example.employee_transport_system.repository.RefreshTokenRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final EmployeeRepository employeeRepo;
    private final AdminRepository adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepo;

    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new BadCredentialsException("Email and password are required");
        }

        String email = request.getEmail().toLowerCase().trim();

        Employee employee = employeeRepo.findByEmail(email).orElse(null);
        if (employee != null) {
            if (passwordEncoder.matches(request.getPassword(),
                    employee.getPassword())) {
                String token = jwtUtil.generateToken(employee.getEmail(),
                        employee.getRole().toUpperCase());
                String refreshToken = createRefreshToken(employee.getEmail());
                return new AuthResponse(token, refreshToken);
            }
        }

        Admin admin = adminRepo.findByEmail(email).orElse(null);
        if (admin != null) {
            if (passwordEncoder.matches(request.getPassword(),
                    admin.getPassword())) {
                String token = jwtUtil.generateToken(admin.getEmail(), "ADMIN");
                String refreshToken = createRefreshToken(admin.getEmail());
                return new AuthResponse(token, refreshToken);
            }
        }

        throw new BadCredentialsException("Invalid email or password");
    }

    @Transactional
    public String createRefreshToken(String email) {
        refreshTokenRepo.deleteByEmail(email);
        RefreshToken rt = new RefreshToken();
        rt.setEmail(email);
        rt.setToken(UUID.randomUUID().toString());
        rt.setExpiryDate(Instant.now().plusMillis(jwtUtil.getRefreshExpirationMs()));
        refreshTokenRepo.save(rt);
        return rt.getToken();
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {
        RefreshToken rt = refreshTokenRepo.findByToken(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        if (rt.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepo.delete(rt);
            throw new BadCredentialsException("Refresh token was expired. Please sign in again.");
        }

        String email = rt.getEmail();
        String role = "EMPLOYEE";
        Employee employee = employeeRepo.findByEmail(email).orElse(null);
        if (employee == null) {
            Admin admin = adminRepo.findByEmail(email).orElse(null);
            if (admin != null) {
                role = "ADMIN";
            } else {
                throw new BadCredentialsException("User not found for token");
            }
        } else {
            role = employee.getRole().toUpperCase();
        }

        String newAccessToken = jwtUtil.generateToken(email, role);
        refreshTokenRepo.delete(rt);
        String newRefreshToken = createRefreshToken(email);
        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    public void register(RegisterRequest request) {
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

    public void createAdmin(RegisterRequest request) {
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

    public AuthService(EmployeeRepository employeeRepo,
                       AdminRepository adminRepo,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RefreshTokenRepository refreshTokenRepo) {
        this.employeeRepo = employeeRepo;
        this.adminRepo = adminRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepo = refreshTokenRepo;
    }

}