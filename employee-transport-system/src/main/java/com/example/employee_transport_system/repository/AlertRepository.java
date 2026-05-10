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

    /**
     * Finds alerts for a specific employee ordered by timestamp.
     * @param employee the employee
     * @return list of user alerts
     */
    List<Alert> findByEmployeeOrderByTimestampDesc(Employee employee);
}
