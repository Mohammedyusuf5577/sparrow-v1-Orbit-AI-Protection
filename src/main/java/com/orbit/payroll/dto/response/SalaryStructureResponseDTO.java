package com.orbit.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryStructureResponseDTO {
    private UUID id;
    private BigDecimal basicSalary;
    private BigDecimal allowance;
    private BigDecimal pf;
    private BigDecimal tax;
    private BigDecimal otherDeduction;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
