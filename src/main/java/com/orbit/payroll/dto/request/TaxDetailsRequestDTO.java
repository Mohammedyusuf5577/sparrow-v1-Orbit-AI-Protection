package com.orbit.payroll.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxDetailsRequestDTO {
    private String pan;
    private String aadhaar;
    private String uan;
}
