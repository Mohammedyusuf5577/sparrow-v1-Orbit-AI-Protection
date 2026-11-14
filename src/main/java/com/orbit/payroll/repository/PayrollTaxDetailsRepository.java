package com.orbit.payroll.repository;

import com.orbit.payroll.entity.PayrollTaxDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PayrollTaxDetailsRepository extends JpaRepository<PayrollTaxDetailsEntity, UUID> {
    void deleteByEmployeePayrollId(UUID employeePayrollId);
}
