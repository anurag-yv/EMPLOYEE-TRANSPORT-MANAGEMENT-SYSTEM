package com.example.employee_transport_system.repository;

import com.example.employee_transport_system.entity.Booking;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    int countByRoute(Route route);

    int countByRouteAndStatus(Route route, String status);

    boolean existsByEmployeeAndRoute(Employee employee, Route route);

    boolean existsByEmployeeAndRouteAndStatus(Employee employee, Route route, String status);

    java.util.List<Booking> findByEmployee(Employee employee);

    java.util.List<Booking> findByEmployeeAndStatus(Employee employee, String status);

    java.util.List<Booking> findByRoute(Route route);
}
