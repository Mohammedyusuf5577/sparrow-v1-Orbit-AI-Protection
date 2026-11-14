package com.orbit.payroll.repository;

import com.orbit.payroll.entity.EmployeePayrollEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmployeePayrollRepository extends JpaRepository<EmployeePayrollEntity, UUID> {
    java.util.Optional<EmployeePayrollEntity> findByEmployeeId(UUID employeeId);
}
