package com.orbit.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Employee payroll ID is required")
    private UUID payrollId;

    /**
     * Amount in INR (rupees). Minimum ₹1.
     * If null, the amount is fetched from payroll salary structure automatically.
     */
    @DecimalMin(value = "1.0", message = "Amount must be at least ₹1")
    private BigDecimal amount;

    private String description;
}
