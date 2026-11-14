package com.orbit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String role;
    private String status;
    private String phone;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Employment
    private String department;
    private String designation;
    private String employmentType;
    private String joinDate;

    // Banking Details (Masked for Security)
    private String accountHolderName;
    private String bankName;
    private String accountNumber;
    private String ifsc;
    private String upiId;

    // Tax & Compliance (Masked for Security)
    private String pan;
    private String aadhaar;
    private String uan;

    // Salary Structure
    private java.math.BigDecimal basicSalary;
    private java.math.BigDecimal allowance;
}
