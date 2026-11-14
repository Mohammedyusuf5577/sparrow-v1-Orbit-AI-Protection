package com.orbit.payout.repository;

import com.orbit.payout.entity.PayoutAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PayoutAuditLogRepository extends JpaRepository<PayoutAuditLogEntity, UUID> {

    List<PayoutAuditLogEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<PayoutAuditLogEntity> findByEntityIdOrderByCreatedAtDesc(UUID entityId);
}
