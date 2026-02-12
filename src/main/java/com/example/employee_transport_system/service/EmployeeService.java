package com.example.employee_transport_system.service;

import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.exception.ResourceNotFoundException;
import com.example.employee_transport_system.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepo;

    public Employee createEmployee(Employee employee) {
        return employeeRepo.save(employee);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepo.findAll();
    }


    public Employee getEmployeeById(Long id) {
        return employeeRepo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Employee not found with id " + id));
    }

    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        Employee emp = getEmployeeById(id);
        emp.setName(updatedEmployee.getName());
        emp.setEmail(updatedEmployee.getEmail());
        emp.setPassword(updatedEmployee.getPassword());
        return employeeRepo.save(emp);
    }

    public void deleteEmployee(Long id) {
        Employee emp = getEmployeeById(id);
        employeeRepo.delete(emp);
    }
}
