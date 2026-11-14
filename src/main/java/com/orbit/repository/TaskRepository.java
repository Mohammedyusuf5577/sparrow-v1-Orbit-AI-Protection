package com.orbit.repository;

import com.orbit.entity.TaskEntity;
import com.orbit.util.AppUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {

    List<TaskEntity> findByProjectId(UUID projectId);

    List<TaskEntity> findByAssignedToId(UUID userId);

    long countByProjectIdAndStatus(UUID projectId, AppUtils.TaskStatus status);
}

