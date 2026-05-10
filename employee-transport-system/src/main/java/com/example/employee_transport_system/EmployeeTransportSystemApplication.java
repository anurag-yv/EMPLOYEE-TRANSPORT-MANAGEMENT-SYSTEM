package com.example.employee_transport_system;

import com.example.employee_transport_system.entity.Admin;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.repository.AdminRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class EmployeeTransportSystemApplication implements CommandLineRunner {

    private final AdminRepository adminRepo;
    private final EmployeeRepository employeeRepo;
    private final PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(EmployeeTransportSystemApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // Seeding is handled in DataSeeder.java
    }

    public EmployeeTransportSystemApplication(AdminRepository adminRepo, EmployeeRepository employeeRepo, PasswordEncoder passwordEncoder) {
        this.adminRepo = adminRepo;
        this.employeeRepo = employeeRepo;
        this.passwordEncoder = passwordEncoder;
    }

}
