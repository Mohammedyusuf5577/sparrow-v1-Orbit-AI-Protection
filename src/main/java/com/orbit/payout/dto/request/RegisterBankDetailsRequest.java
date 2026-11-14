package com.orbit.payout.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RegisterBankDetailsRequest {

    @NotNull(message = "Employee payroll ID is required")
    private UUID employeePayrollId;

    @NotBlank(message = "Account holder name is required")
    private String accountName;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "IFSC code is required")
    private String ifsc;

    private String upiId;
}
