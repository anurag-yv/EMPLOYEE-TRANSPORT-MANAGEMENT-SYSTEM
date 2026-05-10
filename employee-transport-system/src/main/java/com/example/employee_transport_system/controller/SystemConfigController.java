package com.example.employee_transport_system.controller;

import com.example.employee_transport_system.entity.SystemConfig;
import com.example.employee_transport_system.repository.SystemConfigRepository;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing system configurations.
 */
@RestController
@RequestMapping("/api/config")
public class SystemConfigController {

    private final SystemConfigRepository configRepo;

    public SystemConfigController(SystemConfigRepository configRepo) {
        this.configRepo = configRepo;
    }

    @GetMapping
    public SystemConfig getConfig() {
        return configRepo.findById("global").orElseGet(() -> {
            SystemConfig config = new SystemConfig();
            return configRepo.save(config);
        });
    }

    @PutMapping
    public SystemConfig updateConfig(@RequestBody SystemConfig newConfig) {
        newConfig.setId("global");
        return configRepo.save(newConfig);
    }
}
