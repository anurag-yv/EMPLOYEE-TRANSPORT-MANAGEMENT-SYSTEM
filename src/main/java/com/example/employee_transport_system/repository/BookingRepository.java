package com.example.employee_transport_system.repository;

import com.example.employee_transport_system.entity.Booking;
import com.example.employee_transport_system.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    long countByRoute(Route route);
}
