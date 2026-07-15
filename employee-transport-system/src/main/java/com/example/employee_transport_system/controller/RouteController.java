package com.example.employee_transport_system.controller;

import com.example.employee_transport_system.entity.Route;
import com.example.employee_transport_system.repository.RouteRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteRepository routeRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public RouteController(RouteRepository routeRepository, org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.routeRepository = routeRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @jakarta.annotation.PostConstruct
    public void migrateNullVersions() {
        jdbcTemplate.update("UPDATE routes SET version = 0 WHERE version IS NULL");
    }

    @GetMapping
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    @GetMapping("/{id}")
    public Route getRouteById(@PathVariable Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));
    }

    @PostMapping
    public Route addRoute(@Valid @RequestBody Route route) {
        route.setBookedSeats(0);
        return routeRepository.save(route);
    }

    @PutMapping("/{id}")
    public Route updateRoute(@PathVariable Long id, @Valid @RequestBody Route updatedRoute) {
        Route existing = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        existing.setSource(updatedRoute.getSource());
        existing.setDestination(updatedRoute.getDestination());
        existing.setPickupTime(updatedRoute.getPickupTime());
        existing.setCapacity(updatedRoute.getCapacity());
        return routeRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRoute(@PathVariable Long id) {
        if (!routeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        jdbcTemplate.update("DELETE FROM bookings WHERE route_id = ?", id);
        routeRepository.deleteById(id);
        return ResponseEntity.ok("Route deleted successfully");
    }
}
