package com.orbit.payroll.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDetailsRequestDTO {
    private String accountName;
    private String bankName;
    private String accountNumber;
    private String ifsc;
}
