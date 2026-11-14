package com.orbit.payroll.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequestDTO {
    private Integer workingDays;
    private Integer presentDays;
    private Integer leaveDays;
    private BigDecimal overtimeHours;
}
