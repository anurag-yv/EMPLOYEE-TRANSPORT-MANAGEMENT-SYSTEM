package com.example.employee_transport_system.config;

import com.example.employee_transport_system.entity.Admin;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.entity.Route;
import com.example.employee_transport_system.repository.AdminRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import com.example.employee_transport_system.repository.RouteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner loadData(EmployeeRepository empRepo, AdminRepository adminRepo, RouteRepository routeRepo, PasswordEncoder encoder) {
        return args -> {
            System.out.println("[SEEDER] Starting data seeding check...");
            if (empRepo.count() == 0) {
                System.out.println("[SEEDER] Seeding Employee...");
                Employee e = new Employee();
                e.setEmail("employee@example.com");
                e.setName("Test Employee");
                e.setPassword(encoder.encode("password123"));
                e.setRole("EMPLOYEE");
                empRepo.save(e);
            }
            if (adminRepo.count() == 0) {
                System.out.println("[SEEDER] Seeding Admin...");
                Admin a = new Admin();
                a.setEmail("admin@example.com");
                a.setName("Root Admin");
                a.setPassword(encoder.encode("password123"));
                a.setRole("ADMIN");
                adminRepo.save(a);
            }
            if (routeRepo.count() == 0) {
                System.out.println("[SEEDER] Seeding Routes...");
                Route r1 = new Route();
                r1.setSource("Main Office");
                r1.setDestination("Downtown Area");
                r1.setPickupTime("08:00 AM");
                r1.setCapacity(40);
                r1.setBookedSeats(0);
                r1.setBudget(1200.0);
                routeRepo.save(r1);
 
                Route r2 = new Route();
                r2.setSource("South Branch");
                r2.setDestination("Main Office");
                r2.setPickupTime("09:00 AM");
                r2.setCapacity(20);
                r2.setBookedSeats(0);
                r2.setBudget(850.0);
                routeRepo.save(r2);
            }
            System.out.println("[SEEDER] Data Seeding Completed Successfully.");
        };
    }
}
