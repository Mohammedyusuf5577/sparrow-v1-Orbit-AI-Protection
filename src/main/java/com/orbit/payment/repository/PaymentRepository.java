package com.orbit.payment.repository;

import com.orbit.payment.entity.PaymentEntity;
import com.orbit.util.AppUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByRazorpayOrderId(String razorpayOrderId);

    boolean existsByRazorpayOrderIdAndStatus(String razorpayOrderId, AppUtils.PaymentStatus status);

    List<PaymentEntity> findByEmployeePayroll_IdOrderByCreatedAtDesc(UUID payrollId);

    List<PaymentEntity> findByEmployeePayroll_Employee_IdOrderByCreatedAtDesc(UUID employeeId);
}
