package com.orbit.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payroll_salary_structure")
public class PayrollSalaryStructureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_payroll_id", nullable = false)
    private EmployeePayrollEntity employeePayroll;

    @Column(name = "basic_salary")
    private BigDecimal basicSalary;

    @Column(name = "allowance")
    private BigDecimal allowance;

    @Column(name = "pf")
    private BigDecimal pf;

    @Column(name = "tax")
    private BigDecimal tax;

    @Column(name = "other_deduction")
    private BigDecimal otherDeduction;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
