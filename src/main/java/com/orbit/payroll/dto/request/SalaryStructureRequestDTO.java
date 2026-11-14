package com.orbit.payroll.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryStructureRequestDTO {
    private BigDecimal basicSalary;
    private BigDecimal allowance;
    private BigDecimal pf;
    private BigDecimal tax;
    private BigDecimal otherDeduction;
}
