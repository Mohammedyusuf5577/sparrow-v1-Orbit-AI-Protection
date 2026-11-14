package com.orbit.payout.entity;

import com.orbit.entity.UserEntity;
import com.orbit.util.AppUtils;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an actual Razorpay payout transaction linked to a payout record.
 * Multiple transactions can exist per record (retries).
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payout_transaction")
public class PayoutTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payout_record_id", nullable = false)
    private PayoutRecordEntity payoutRecord;

    @Column(name = "razorpay_payout_id")
    private String razorpayPayoutId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", length = 20)
    @Builder.Default
    private AppUtils.PayoutMode mode = AppUtils.PayoutMode.IMPS;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private AppUtils.PayoutStatus status = AppUtils.PayoutStatus.PENDING;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "fund_account_id")
    private String fundAccountId;

    @Column(name = "narration")
    private String narration;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
