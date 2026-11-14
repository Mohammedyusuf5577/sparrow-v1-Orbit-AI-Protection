package com.orbit.repository;

import com.orbit.entity.UserEntity;
import com.orbit.util.AppUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    // Keep original for internal exact-match use
    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findByRole(AppUtils.UserRole role);

    List<UserEntity> findByRoleAndStatus(AppUtils.UserRole role, AppUtils.UserStatus status);

    long countByRole(AppUtils.UserRole role);

    long countByRoleAndStatus(AppUtils.UserRole role, AppUtils.UserStatus status);

    long countByStatus(AppUtils.UserStatus status);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);
}

