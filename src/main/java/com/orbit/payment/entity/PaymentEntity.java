package com.orbit.payment.entity;

import com.orbit.payroll.entity.EmployeePayrollEntity;
import com.orbit.util.AppUtils;
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
@Table(name = "payments",
        indexes = {
                @Index(name = "idx_payment_razorpay_order_id", columnList = "razorpay_order_id"),
                @Index(name = "idx_payment_employee_payroll_id", columnList = "employee_payroll_id")
        })
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_payroll_id", nullable = false)
    private EmployeePayrollEntity employeePayroll;

    @Column(name = "razorpay_order_id", unique = true, nullable = false)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature")
    private String razorpaySignature;

    /**
     * Amount stored in PAISE (INR cents). ₹1 = 100 paise.
     * Razorpay requires integer paise. We convert BigDecimal rupees * 100.
     */
    @Column(name = "amount_paise", nullable = false)
    private Long amountPaise;

    /**
     * Human-readable amount in INR (rupees).
     */
    @Column(name = "amount_inr", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountInr;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AppUtils.PaymentStatus status = AppUtils.PaymentStatus.CREATED;

    @Column(name = "currency", nullable = false)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "description")
    private String description;

    @Column(name = "failure_reason")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
