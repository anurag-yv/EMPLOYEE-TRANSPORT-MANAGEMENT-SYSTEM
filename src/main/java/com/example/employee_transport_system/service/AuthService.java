package com.example.employee_transport_system.service;

import com.example.employee_transport_system.config.JwtUtil;
import com.example.employee_transport_system.dto.AuthRequest;
import com.example.employee_transport_system.dto.AuthResponse;
import com.example.employee_transport_system.entity.Admin;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.repository.AdminRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.BadCredentialsException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepo;
    private final AdminRepository adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse authenticate(AuthRequest request) {

        Employee employee = employeeRepo.findByEmail(request.getEmail())
                .orElse(null);

        if (employee != null && passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            String token = jwtUtil.generateToken(employee.getEmail(), "EMPLOYEE");
            return new AuthResponse(token);
        }

        Admin admin = adminRepo.findByEmail(request.getEmail())
                .orElse(null);

        if (admin != null && passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            String token = jwtUtil.generateToken(admin.getEmail(), "ADMIN");
            return new AuthResponse(token);
        }


        throw new BadCredentialsException("Invalid email or password");
    }
}
