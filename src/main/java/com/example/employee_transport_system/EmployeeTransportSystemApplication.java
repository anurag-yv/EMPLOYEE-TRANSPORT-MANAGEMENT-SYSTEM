package com.example.employee_transport_system;

import com.example.employee_transport_system.entity.Admin;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.repository.AdminRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration.class
        }
)
@RequiredArgsConstructor
public class EmployeeTransportSystemApplication implements CommandLineRunner {

    private final AdminRepository adminRepo;
    private final EmployeeRepository employeeRepo;
    private final PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(EmployeeTransportSystemApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed Admin
        if (adminRepo.findByEmail("admin@test.com").isEmpty()) {
            Admin admin = new Admin();
            admin.setEmail("admin@test.com");
            admin.setPassword(passwordEncoder.encode("1234"));
            adminRepo.save(admin);
        }

        if (employeeRepo.findByEmail("employee@test.com").isEmpty()) {
            Employee emp = new Employee();
            emp.setEmail("employee@test.com");
            emp.setName("John Doe");
            emp.setPassword(passwordEncoder.encode("1234"));
            employeeRepo.save(emp);
        }
    }
}
