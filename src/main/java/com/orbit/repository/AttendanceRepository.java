package com.orbit.repository;

import com.orbit.entity.AttendanceEntity;
import com.orbit.util.AppUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, UUID> {

    List<AttendanceEntity> findByUserIdOrderByDateDesc(UUID userId);

    List<AttendanceEntity> findByDateOrderByUserFullNameAsc(LocalDate date);

    List<AttendanceEntity> findAllByOrderByDateDesc();

    long countByDateAndStatus(LocalDate date, AppUtils.AttendanceStatus status);

    long countByStatus(AppUtils.AttendanceStatus status);
}

