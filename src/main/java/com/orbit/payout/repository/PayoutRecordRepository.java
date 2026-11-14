package com.orbit.payout.repository;

import com.orbit.payout.entity.PayoutRecordEntity;
import com.orbit.util.AppUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PayoutRecordRepository extends JpaRepository<PayoutRecordEntity, UUID> {

    List<PayoutRecordEntity> findByUserId(UUID userId);

    List<PayoutRecordEntity> findByStatus(AppUtils.PayoutStatus status);

    @Query("SELECT p FROM PayoutRecordEntity p WHERE p.salaryDate = :date AND p.status = :status")
    List<PayoutRecordEntity> findBySalaryDateAndStatus(
            @Param("date") LocalDate date,
            @Param("status") AppUtils.PayoutStatus status);

    @Query("SELECT p FROM PayoutRecordEntity p WHERE p.salaryDate <= :today AND p.status = 'PENDING'")
    List<PayoutRecordEntity> findPendingPayoutsUpToDate(@Param("today") LocalDate today);

    boolean existsByUserIdAndSalaryDate(UUID userId, LocalDate salaryDate);
}
