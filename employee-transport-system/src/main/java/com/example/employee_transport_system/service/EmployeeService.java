package com.example.employee_transport_system.service;

import com.example.employee_transport_system.dto.EmployeeResponseDTO;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.repository.EmployeeRepository;
import com.example.employee_transport_system.repository.BookingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BookingRepository bookingRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository,
                           BookingRepository bookingRepository,
                           org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private EmployeeResponseDTO toDTO(Employee emp) {
        return new EmployeeResponseDTO(emp.getId(), emp.getName(), emp.getEmail(), emp.getRole());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<EmployeeResponseDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(this::toDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Optional<EmployeeResponseDTO> getEmployeeById(Long id) {
        return employeeRepository.findById(id).map(this::toDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Employee saveEmployee(Employee employee) {
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        return employeeRepository.save(employee);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee != null) {
            bookingRepository.deleteAll(bookingRepository.findByEmployee(employee));
            employeeRepository.deleteById(id);
        }
    }
}