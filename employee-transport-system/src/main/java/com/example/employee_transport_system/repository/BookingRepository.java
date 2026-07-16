package com.example.employee_transport_system.repository;

import com.example.employee_transport_system.entity.Booking;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.entity.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Override
    @EntityGraph(attributePaths = {"employee", "route"})
    List<Booking> findAll();

    @Override
    @EntityGraph(attributePaths = {"employee", "route"})
    Page<Booking> findAll(Pageable pageable);

    int countByRoute(Route route);

    int countByRouteAndStatus(Route route, String status);

    boolean existsByEmployeeAndRoute(Employee employee, Route route);

    boolean existsByEmployeeAndRouteAndStatus(Employee employee, Route route, String status);

    @EntityGraph(attributePaths = {"employee", "route"})
    List<Booking> findByEmployee(Employee employee);

    @EntityGraph(attributePaths = {"employee", "route"})
    Page<Booking> findByEmployee(Employee employee, Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "route"})
    List<Booking> findByEmployeeAndStatus(Employee employee, String status);

    @EntityGraph(attributePaths = {"employee", "route"})
    List<Booking> findByRoute(Route route);

    @EntityGraph(attributePaths = {"employee", "route"})
    List<Booking> findByRouteId(Long routeId);

    @EntityGraph(attributePaths = {"employee", "route"})
    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT b.bookedAt FROM Booking b WHERE b.bookedAt IS NOT NULL")
    List<LocalDateTime> findAllBookedAtDates();
}
