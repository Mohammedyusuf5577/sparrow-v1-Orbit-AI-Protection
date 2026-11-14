package com.orbit.dto.request;

import com.orbit.util.AppUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = AppUtils.Constants.EMAIL_REQUIRED)
    @Email(message = AppUtils.Constants.INVALID_EMAIL)
    private String email;

    @NotBlank(message = AppUtils.Constants.FULL_NAME_REQUIRED)
    private String fullName;

    private String password;

    @NotNull(message = AppUtils.Constants.ROLE_REQUIRED)
    private AppUtils.UserRole role;

    @NotNull(message = AppUtils.Constants.STATUS_REQUIRED)
    private AppUtils.UserStatus status;

    private String phone;

    // Employment Details
    private String department;
    private String designation;
    private String employmentType;
    private String joinDate;

    // Banking Details for Salary Crediting (Secure Payouts)
    private String accountHolderName;
    private String bankName;
    private String accountNumber;
    private String ifsc;
    private String upiId;

    // Tax & Compliance Details
    private String pan;
    private String aadhaar;
    private String uan;

    // Salary Structure
    private java.math.BigDecimal basicSalary;
    private java.math.BigDecimal allowance;
}
