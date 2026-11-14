package com.orbit.payment.dto.response;

import com.orbit.util.AppUtils;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {

    private UUID id;
    private UUID payrollId;
    private String employeeName;
    private String employeeEmail;

    private String razorpayOrderId;
    private String razorpayPaymentId;

    private BigDecimal amountInr;
    private String currency;
    private String description;

    private AppUtils.PaymentStatus status;
    private String failureReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
