package com.orbit.repository;

import com.orbit.entity.ProjectAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignmentEntity, UUID> {

    List<ProjectAssignmentEntity> findByProjectId(UUID projectId);

    @Query("SELECT pa FROM ProjectAssignmentEntity pa JOIN FETCH pa.instructor WHERE pa.project.id = :projectId")
    List<ProjectAssignmentEntity> findByProjectIdWithUser(@Param("projectId") UUID projectId);

    List<ProjectAssignmentEntity> findByInstructorId(UUID instructorId);

    boolean existsByProjectIdAndInstructorId(UUID projectId, UUID instructorId);

    @Modifying
    @Query("DELETE FROM ProjectAssignmentEntity pa WHERE pa.project.id = :projectId AND pa.instructor.id IN :instructorIds")
    void deleteByProjectIdAndInstructorIds(@Param("projectId") UUID projectId, @Param("instructorIds") Collection<UUID> instructorIds);

    @Modifying
    @Query("DELETE FROM ProjectAssignmentEntity pa WHERE pa.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);
}
