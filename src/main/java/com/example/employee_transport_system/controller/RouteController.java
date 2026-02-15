package com.example.employee_transport_system.controller;

import com.example.employee_transport_system.entity.Route;
import com.example.employee_transport_system.repository.RouteRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
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
        return routeRepository.save(route);
    }
}
