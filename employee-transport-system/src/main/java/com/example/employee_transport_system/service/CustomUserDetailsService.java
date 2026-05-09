package com.example.employee_transport_system.service;

import com.example.employee_transport_system.entity.Admin;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.repository.AdminRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepo;
    private final EmployeeRepository employeeRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Employee emp = employeeRepo.findByEmail(email).orElse(null);
        if (emp != null) {
            return new User(emp.getEmail(), emp.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        }

        Admin admin = adminRepo.findByEmail(email).orElse(null);
        if (admin != null) {
            return new User(admin.getEmail(), admin.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        }

        throw new UsernameNotFoundException("User not found: " + email);
    }

    public CustomUserDetailsService(AdminRepository adminRepo, EmployeeRepository employeeRepo) {
        this.adminRepo = adminRepo;
        this.employeeRepo = employeeRepo;
    }

}
