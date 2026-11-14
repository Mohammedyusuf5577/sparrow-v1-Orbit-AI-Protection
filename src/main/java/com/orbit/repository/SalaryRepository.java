package com.orbit.repository;

import com.orbit.entity.SalaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SalaryRepository extends JpaRepository<SalaryEntity, UUID> {

    List<SalaryEntity> findByUserIdOrderByPaymentDateDesc(UUID userId);

    List<SalaryEntity> findAllByOrderByPaymentDateDesc();
}

