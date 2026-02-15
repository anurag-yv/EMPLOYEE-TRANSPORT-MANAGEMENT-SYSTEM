package com.example.employee_transport_system.controller;

import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable Long id) {
        return employeeService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Employee> addEmployee(
            @Valid @RequestBody Employee employee
    ) {
        // encode password
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        Employee saved = employeeService.saveEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
