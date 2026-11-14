package com.orbit.payout.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreatePayoutRecordRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Salary amount is required")
    @DecimalMin(value = "1.00", message = "Salary amount must be at least 1.00")
    private BigDecimal salaryAmount;

    @NotNull(message = "Salary date is required")
    @FutureOrPresent(message = "Salary date cannot be in the past")
    private LocalDate salaryDate;
}
