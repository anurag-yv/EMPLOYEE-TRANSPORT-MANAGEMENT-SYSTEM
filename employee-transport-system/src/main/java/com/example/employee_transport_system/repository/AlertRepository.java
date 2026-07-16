package com.example.employee_transport_system.repository;

import com.example.employee_transport_system.entity.Alert;
import com.example.employee_transport_system.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository for managing Alert entities.
 */
public interface AlertRepository extends JpaRepository<Alert, Long> {
    /**
     * Finds unresolved alerts ordered by timestamp.
     * @return list of active alerts
     */
    List<Alert> findByResolvedFalseOrderByTimestampDesc();

    org.springframework.data.domain.Page<Alert> findByResolvedFalseOrderByTimestampDesc(
            org.springframework.data.domain.Pageable pageable);

    List<Alert> findByEmployeeOrderByTimestampDesc(Employee employee);

    org.springframework.data.domain.Page<Alert> findByEmployeeOrderByTimestampDesc(
            Employee employee, org.springframework.data.domain.Pageable pageable);
}
