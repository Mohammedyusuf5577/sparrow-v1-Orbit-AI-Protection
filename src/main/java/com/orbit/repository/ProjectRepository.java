package com.orbit.repository;

import com.orbit.entity.ProjectEntity;
import com.orbit.util.AppUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID>, JpaSpecificationExecutor<ProjectEntity> {

    List<ProjectEntity> findByStatus(AppUtils.ProjectStatus status);

    long countByStatus(AppUtils.ProjectStatus status);

    @Query("SELECT p FROM ProjectEntity p WHERE p.endDate < :now AND p.status <> :completedStatus")
    List<ProjectEntity> findDelayedProjects(@Param("now") LocalDateTime now,
            @Param("completedStatus") AppUtils.ProjectStatus completedStatus);

    @Modifying
    @Query("UPDATE ProjectEntity p SET p.deletedAt = :now WHERE p.id = :id AND p.deletedAt IS NULL")
    int softDeleteById(@Param("id") UUID id, @Param("now") LocalDateTime now);

    @Query("SELECT pa.project.id, COUNT(pa.id) FROM ProjectAssignmentEntity pa WHERE pa.project.id IN :projectIds GROUP BY pa.project.id")
    List<Object[]> countAssignmentsByProjectIds(@Param("projectIds") List<UUID> projectIds);

    @Query(value = "SELECT EXTRACT(MONTH FROM start_date) AS m, " +
            "COUNT(id) AS total, " +
            "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed, " +
            "SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS in_progress, " +
            "SUM(CASE WHEN status = 'DELAYED' THEN 1 ELSE 0 END) AS delayed " +
            "FROM projects " +
            "WHERE EXTRACT(YEAR FROM start_date) = :year AND deleted_at IS NULL " +
            "GROUP BY EXTRACT(MONTH FROM start_date)", nativeQuery = true)
    List<Object[]> getMonthlyStatsNative(@Param("year") int year);

    @Query(value = "SELECT " +
            "COUNT(id) AS total, " +
            "COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS completed, " +
            "COALESCE(SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END), 0) AS in_progress, " +
            "COALESCE(SUM(CASE WHEN status = 'DELAYED' THEN 1 ELSE 0 END), 0) AS delayed " +
            "FROM projects WHERE deleted_at IS NULL", nativeQuery = true)
    List<Object[]> getProjectStatusCounts();
}
