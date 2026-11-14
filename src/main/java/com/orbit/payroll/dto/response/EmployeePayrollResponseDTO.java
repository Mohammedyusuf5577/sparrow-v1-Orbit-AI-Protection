package com.orbit.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePayrollResponseDTO {
    private UUID id;
    private UUID employeeId;
    private String name;
    private String mobile;
    private String email;
    private LocalDate joinDate;
    private String department;
    private String designation;
    private String employmentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private BigDecimal netSalary;

    private BankDetailsResponseDTO bankDetails;
    private TaxDetailsResponseDTO taxDetails;
    private SalaryStructureResponseDTO salaryStructure;
    private AttendanceResponseDTO attendance;
}
