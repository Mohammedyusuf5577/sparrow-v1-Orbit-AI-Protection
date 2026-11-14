package com.orbit.payroll.entity;

import com.orbit.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employee_payroll")
public class EmployeePayrollEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private UserEntity employee;

    @Column(name = "name")
    private String name;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "email")
    private String email;

    @Column(name = "join_date")
    private LocalDate joinDate;

    @Column(name = "department")
    private String department;

    @Column(name = "designation")
    private String designation;

    @Column(name = "employment_type")
    private String employmentType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "employeePayroll", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PayrollBankDetailsEntity bankDetails;

    @OneToOne(mappedBy = "employeePayroll", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PayrollTaxDetailsEntity taxDetails;

    @OneToOne(mappedBy = "employeePayroll", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PayrollSalaryStructureEntity salaryStructure;

    @OneToOne(mappedBy = "employeePayroll", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PayrollAttendanceEntity attendance;
}
