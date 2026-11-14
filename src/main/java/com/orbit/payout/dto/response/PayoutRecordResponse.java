package com.orbit.payout.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.orbit.util.AppUtils;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayoutRecordResponse {
    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private String employeeEmail;
    private BigDecimal salaryAmount;
    private LocalDate salaryDate;
    private AppUtils.PayoutStatus status;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
