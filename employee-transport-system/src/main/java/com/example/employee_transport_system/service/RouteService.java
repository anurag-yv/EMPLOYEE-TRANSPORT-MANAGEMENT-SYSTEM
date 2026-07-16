package com.example.employee_transport_system.service;

import com.example.employee_transport_system.entity.Route;
import com.example.employee_transport_system.exception.ResourceNotFoundException;
import com.example.employee_transport_system.repository.RouteRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final JdbcTemplate jdbcTemplate;

    public RouteService(RouteRepository routeRepository, JdbcTemplate jdbcTemplate) {
        this.routeRepository = routeRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // One-time migration for existing routes that have null version values
    @PostConstruct
    @Transactional
    public void migrateNullVersions() {
        jdbcTemplate.update("UPDATE routes SET version = 0 WHERE version IS NULL");
    }

    @Cacheable(value = "routes", key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Route> getAllRoutes(Pageable pageable) {
        return routeRepository.findAll(pageable);
    }

    public List<Route> getAllRoutesList() {
        return routeRepository.findAll();
    }

    @Cacheable(value = "routes", key = "#id")
    public Route getRouteById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));
    }

    @Transactional
    @CacheEvict(value = "routes", allEntries = true)
    public Route addRoute(Route route) {
        route.setBookedSeats(0);
        return routeRepository.save(route);
    }

    @Transactional
    @CacheEvict(value = "routes", allEntries = true)
    public Route updateRoute(Long id, Route updatedRoute) {
        Route existing = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));
        existing.setSource(updatedRoute.getSource());
        existing.setDestination(updatedRoute.getDestination());
        existing.setPickupTime(updatedRoute.getPickupTime());
        existing.setCapacity(updatedRoute.getCapacity());
        return routeRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = "routes", allEntries = true)
    public boolean deleteRoute(Long id) {
        if (!routeRepository.existsById(id)) {
            return false;
        }
        // Cascade delete via JDBC to avoid entity loading overhead
        jdbcTemplate.update("DELETE FROM bookings WHERE route_id = ?", id);
        routeRepository.deleteById(id);
        return true;
    }
}