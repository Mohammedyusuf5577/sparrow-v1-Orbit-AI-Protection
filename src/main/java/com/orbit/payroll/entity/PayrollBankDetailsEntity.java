package com.orbit.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payroll_bank_details")
public class PayrollBankDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_payroll_id", nullable = false)
    private EmployeePayrollEntity employeePayroll;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber; // stored encrypted

    @Column(name = "ifsc")
    private String ifsc;

    @Column(name = "upi_id")
    private String upiId;

    @Column(name = "razorpay_contact_id")
    private String razorpayContactId;

    @Column(name = "razorpay_fund_account_id")
    private String razorpayFundAccountId;

    @Builder.Default
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

