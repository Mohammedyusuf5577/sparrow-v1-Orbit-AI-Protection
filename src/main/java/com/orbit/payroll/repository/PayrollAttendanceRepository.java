package com.orbit.payroll.repository;

import com.orbit.payroll.entity.PayrollAttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PayrollAttendanceRepository extends JpaRepository<PayrollAttendanceEntity, UUID> {
    void deleteByEmployeePayrollId(UUID employeePayrollId);
}
