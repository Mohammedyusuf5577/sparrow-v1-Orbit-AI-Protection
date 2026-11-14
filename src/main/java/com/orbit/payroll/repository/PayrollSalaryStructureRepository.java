package com.orbit.payroll.repository;

import com.orbit.payroll.entity.PayrollSalaryStructureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollSalaryStructureRepository extends JpaRepository<PayrollSalaryStructureEntity, UUID> {
    void deleteByEmployeePayrollId(UUID employeePayrollId);

    Optional<PayrollSalaryStructureEntity> findByEmployeePayroll_Id(UUID payrollId);
}

