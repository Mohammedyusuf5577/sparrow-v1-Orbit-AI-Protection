package com.orbit.payroll.dto.request;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EmployeePayrollRequestDTO {
    private UUID employeeId;
    private String name;
    private String mobile;
    private String email;
    private LocalDate joinDate;
    private String department;
    private String designation;
    private String employmentType;
}
