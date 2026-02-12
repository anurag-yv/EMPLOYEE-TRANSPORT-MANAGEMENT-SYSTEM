package com.example.employee_transport_system.service;

import com.example.employee_transport_system.dto.RouteDTO;
import com.example.employee_transport_system.entity.Route;
import com.example.employee_transport_system.exception.ResourceNotFoundException;
import com.example.employee_transport_system.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepo;

    public List<Route> getAllRoutes() {
        return routeRepo.findAll();
    }

    public Route createRoute(RouteDTO dto) {
        Route route = Route.builder()
                .fromLocation(dto.getFrom())
                .toLocation(dto.getTo())
                .capacity(dto.getCapacity())
                .build();
        return routeRepo.save(route);
    }

    public Route getRouteById(Long id) {
        return routeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id " + id));
    }
}
