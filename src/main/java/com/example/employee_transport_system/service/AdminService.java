package com.example.employee_transport_system.service;

import com.example.employee_transport_system.entity.Admin;
import com.example.employee_transport_system.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepo;

    public List<Admin> getAllAdmins() {
        return adminRepo.findAll();
    }

    public Admin createAdmin(Admin admin) {
        return adminRepo.save(admin);
    }
}
