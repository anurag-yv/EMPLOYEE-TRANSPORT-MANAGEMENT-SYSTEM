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

    public RouteController(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
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
        routeRepository.deleteById(id);
        return ResponseEntity.ok("Route deleted successfully");
    }
}
