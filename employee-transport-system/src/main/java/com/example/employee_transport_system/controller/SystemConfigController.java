package com.example.employee_transport_system.controller;

import com.example.employee_transport_system.entity.SystemConfig;
import com.example.employee_transport_system.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
@Tag(name = "System Configuration", description = "Endpoints for managing global settings (Admin only)")
public class SystemConfigController {

    private final SystemConfigService configService;

    public SystemConfigController(SystemConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    @Operation(summary = "Get the global system configuration")
    public ResponseEntity<SystemConfig> getConfig() {
        return ResponseEntity.ok(configService.getConfig());
    }

    @PutMapping
    @Operation(summary = "Update the global system configuration")
    public ResponseEntity<SystemConfig> updateConfig(@RequestBody SystemConfig newConfig) {
        return ResponseEntity.ok(configService.updateConfig(newConfig));
    }
}