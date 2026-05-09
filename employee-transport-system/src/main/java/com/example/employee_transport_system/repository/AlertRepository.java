package com.example.employee_transport_system.repository;

import com.example.employee_transport_system.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByResolvedFalseOrderByTimestampDesc();
}
