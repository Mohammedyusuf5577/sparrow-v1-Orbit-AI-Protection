package com.orbit.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponseDTO {
    private UUID id;
    private Integer workingDays;
    private Integer presentDays;
    private Integer leaveDays;
    private BigDecimal overtimeHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
