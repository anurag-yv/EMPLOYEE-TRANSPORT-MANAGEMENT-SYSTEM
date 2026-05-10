package com.example.employee_transport_system.repository;

import com.example.employee_transport_system.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for SystemConfig.
 */
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {
}
