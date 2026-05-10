package com.example.employee_transport_system.service;

import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing employee records.
 */
@Service
public final class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Get all employees.
     */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * Get employee by ID.
     */
    public Optional<Employee> getEmployeeById(final Long id) {
        return employeeRepository.findById(id);
    }

    /**
     * Save or update employee.
     */
    public Employee saveEmployee(final Employee employee) {
        return employeeRepository.save(employee);
    }

    /**
     * Delete employee by ID.
     */
    public void deleteEmployee(final Long id) {
        employeeRepository.deleteById(id);
    }
}
