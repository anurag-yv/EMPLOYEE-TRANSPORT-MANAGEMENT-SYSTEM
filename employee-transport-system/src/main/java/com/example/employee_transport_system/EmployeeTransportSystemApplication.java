package com.example.employee_transport_system;

import com.example.employee_transport_system.repository.AdminRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@org.springframework.data.jpa.repository.config.EnableJpaAuditing
@org.springframework.cache.annotation.EnableCaching
public class EmployeeTransportSystemApplication implements CommandLineRunner {

    private final AdminRepository adminRepo;
    private final EmployeeRepository employeeRepo;
    private final PasswordEncoder passwordEncoder;

    public static void main(final String[] args) {
        SpringApplication.run(EmployeeTransportSystemApplication.class, args);
    }

    @Override
    public void run(final String... args) {
        // Seeding is handled in DataSeeder.java
    }

    public EmployeeTransportSystemApplication(final AdminRepository adminRepo,
                                              final EmployeeRepository employeeRepo,
                                              final PasswordEncoder passwordEncoder) {
        this.adminRepo = adminRepo;
        this.employeeRepo = employeeRepo;
        this.passwordEncoder = passwordEncoder;
    }
}
