package com.example.employee_transport_system.controller;

import com.example.employee_transport_system.entity.Route;
import com.example.employee_transport_system.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
@Tag(name = "Route Management", description = "Endpoints for managing transport routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    @Operation(summary = "Get all routes (supports optional pagination and sorting)")
    public ResponseEntity<?> getAllRoutes(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        if (page == null || size == null) {
            return ResponseEntity.ok(routeService.getAllRoutesList());
        }
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(routeService.getAllRoutes(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get route details by ID")
    public ResponseEntity<Route> getRouteById(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @PostMapping
    @Operation(summary = "Create/add a new route")
    public ResponseEntity<Route> addRoute(@Valid @RequestBody Route route) {
        Route saved = routeService.addRoute(route);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing route")
    public ResponseEntity<Route> updateRoute(@PathVariable Long id,
                                             @Valid @RequestBody Route updatedRoute) {
        return ResponseEntity.ok(routeService.updateRoute(id, updatedRoute));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete route by ID")
    public ResponseEntity<String> deleteRoute(@PathVariable Long id) {
        boolean deleted = routeService.deleteRoute(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Route deleted successfully");
    }
}