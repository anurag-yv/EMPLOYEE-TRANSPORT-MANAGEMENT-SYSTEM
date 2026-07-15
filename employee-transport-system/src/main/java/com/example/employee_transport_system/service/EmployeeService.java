package com.example.employee_transport_system.service;

import com.example.employee_transport_system.dto.EmployeeResponseDTO;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.repository.EmployeeRepository;
import com.example.employee_transport_system.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private EmployeeResponseDTO toDTO(final Employee emp) {
        return new EmployeeResponseDTO(emp.getId(), emp.getName(), emp.getEmail(), emp.getRole());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<EmployeeResponseDTO> getAllEmployees(final int page, final int size) {
        return employeeRepository.findAll(PageRequest.of(page, size))
                .map(this::toDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Optional<EmployeeResponseDTO> getEmployeeById(final Long id) {
        return employeeRepository.findById(id).map(this::toDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Employee saveEmployee(final Employee employee) {
        return employeeRepository.save(employee);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEmployee(final Long id) {
        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee != null) {
            bookingRepository.deleteAll(bookingRepository.findByEmployee(employee));
            employeeRepository.deleteById(id);
        }
    }
}
