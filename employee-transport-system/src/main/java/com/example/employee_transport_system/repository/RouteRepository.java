package com.example.employee_transport_system.repository;

import com.example.employee_transport_system.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {
}
