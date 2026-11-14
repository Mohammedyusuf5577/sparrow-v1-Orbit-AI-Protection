package com.orbit.repository;

import com.orbit.entity.LeaveRequestEntity;
import com.orbit.util.AppUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequestEntity, UUID> {

    List<LeaveRequestEntity> findByStatus(AppUtils.LeaveStatus status);

    List<LeaveRequestEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<LeaveRequestEntity> findAllByOrderByCreatedAtDesc();

    long countByStatus(AppUtils.LeaveStatus status);
}

