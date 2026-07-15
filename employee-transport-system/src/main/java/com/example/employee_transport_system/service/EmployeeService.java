package com.example.employee_transport_system.service;

import com.example.employee_transport_system.dto.EmployeeResponseDTO;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing employee records.
 */
@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Maps an Employee entity to a DTO (without password).
     */
    private EmployeeResponseDTO toDTO(final Employee emp) {
        return new EmployeeResponseDTO(emp.getId(), emp.getName(), emp.getEmail(), emp.getRole());
    }

    /**
     * Get all employees as DTOs with pagination.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Page<EmployeeResponseDTO> getAllEmployees(final int page, final int size) {
        return employeeRepository.findAll(PageRequest.of(page, size))
                .map(this::toDTO);
    }

    /**
     * Get employee by ID as DTO.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Optional<EmployeeResponseDTO> getEmployeeById(final Long id) {
        return employeeRepository.findById(id).map(this::toDTO);
    }

    /**
     * Save or update employee.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Employee saveEmployee(final Employee employee) {
        return employeeRepository.save(employee);
    }

    /**
     * Delete employee by ID.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEmployee(final Long id) {
        employeeRepository.deleteById(id);
    }
}
