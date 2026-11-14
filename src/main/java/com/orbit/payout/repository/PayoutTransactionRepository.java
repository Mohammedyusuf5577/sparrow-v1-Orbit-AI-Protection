package com.orbit.payout.repository;

import com.orbit.payout.entity.PayoutTransactionEntity;
import com.orbit.util.AppUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayoutTransactionRepository extends JpaRepository<PayoutTransactionEntity, UUID> {

    List<PayoutTransactionEntity> findByUserId(UUID userId);

    List<PayoutTransactionEntity> findByPayoutRecordId(UUID payoutRecordId);

    List<PayoutTransactionEntity> findByStatus(AppUtils.PayoutStatus status);

    Optional<PayoutTransactionEntity> findByRazorpayPayoutId(String razorpayPayoutId);

    List<PayoutTransactionEntity> findByStatusAndRetryCountLessThan(
            AppUtils.PayoutStatus status, int maxRetries);
}
