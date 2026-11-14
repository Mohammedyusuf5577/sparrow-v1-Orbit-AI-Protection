package com.orbit.repository;

import com.orbit.entity.NotificationOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationOutboxRepository extends JpaRepository<NotificationOutboxEntity, UUID> {

    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM NotificationOutboxEntity n " +
           "WHERE n.user.id = :userId AND n.project.id = :projectId AND n.status = :status")
    boolean existsByUserIdAndProjectIdAndStatus(@Param("userId") UUID userId,
                                                @Param("projectId") UUID projectId,
                                                @Param("status") String status);

    @Query(value = "SELECT * FROM notification_outbox " +
                   "WHERE status = 'PENDING' AND (next_retry_at IS NULL OR next_retry_at <= :now) " +
                   "ORDER BY created_at ASC LIMIT 50 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<NotificationOutboxEntity> findPendingForProcessing(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE NotificationOutboxEntity n SET n.status = 'SENT', n.sentAt = :now, n.attempts = n.attempts + 1, n.updatedAt = :now WHERE n.id = :id")
    void markSent(@Param("id") UUID id, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE NotificationOutboxEntity n SET n.status = :status, n.nextRetryAt = :nextRetryAt, n.errorLog = :errorLog, n.attempts = n.attempts + 1, n.updatedAt = :now WHERE n.id = :id")
    void updateFailure(@Param("id") UUID id,
                       @Param("status") String status,
                       @Param("nextRetryAt") LocalDateTime nextRetryAt,
                       @Param("errorLog") String errorLog,
                       @Param("now") LocalDateTime now);
}
