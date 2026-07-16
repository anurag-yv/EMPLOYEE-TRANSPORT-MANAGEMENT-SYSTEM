package com.example.employee_transport_system.service;

import com.example.employee_transport_system.entity.SystemConfig;
import com.example.employee_transport_system.repository.SystemConfigRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemConfigService {

    private final SystemConfigRepository configRepo;

    public SystemConfigService(SystemConfigRepository configRepo) {
        this.configRepo = configRepo;
    }

    // Cache global config since it's read frequently but updated rarely
    @Cacheable(value = "config", key = "'global'")
    public SystemConfig getConfig() {
        return configRepo.findById("global").orElseGet(() -> {
            SystemConfig config = new SystemConfig();
            return configRepo.save(config);
        });
    }

    @Transactional
    @CacheEvict(value = "config", key = "'global'")
    public SystemConfig updateConfig(SystemConfig newConfig) {
        newConfig.setId("global");
        return configRepo.save(newConfig);
    }
}