package com.orbit.payment.dto.response;

import com.orbit.util.AppUtils;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentOrderResponse {

    private UUID id;
    private UUID payrollId;
    private String employeeName;
    private String employeeEmail;

    // Razorpay fields needed by frontend checkout
    private String razorpayOrderId;
    private BigDecimal amountInr;
    private Long amountPaise;
    private String currency;
    private String description;

    private AppUtils.PaymentStatus status;
    private LocalDateTime createdAt;
}
