package com.example.employee_transport_system.service;

import com.example.employee_transport_system.config.JwtUtil;
import com.example.employee_transport_system.dto.AuthRequest;
import com.example.employee_transport_system.dto.RegisterRequest;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.repository.AdminRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private EmployeeRepository employeeRepo;

    @Mock
    private AdminRepository adminRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterNewEmployee() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRole("EMPLOYEE");

        when(employeeRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(adminRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        assertDoesNotThrow(() -> authService.register(request));

        verify(employeeRepo, times(1)).save(any(Employee.class));
    }
}
