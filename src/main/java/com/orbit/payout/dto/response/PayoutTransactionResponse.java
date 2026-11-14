package com.orbit.payout.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.orbit.util.AppUtils;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayoutTransactionResponse {
    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private UUID payoutRecordId;
    private String razorpayPayoutId;
    private BigDecimal amount;
    private AppUtils.PayoutMode mode;
    private AppUtils.PayoutStatus status;
    private String referenceId;
    private String fundAccountId;
    private String narration;
    private String errorMessage;
    private String failureReason;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
