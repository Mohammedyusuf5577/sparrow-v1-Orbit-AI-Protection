package com.orbit.payroll.repository;

import com.orbit.payroll.entity.PayrollBankDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PayrollBankDetailsRepository extends JpaRepository<PayrollBankDetailsEntity, UUID> {
    void deleteByEmployeePayrollId(UUID employeePayrollId);
}
