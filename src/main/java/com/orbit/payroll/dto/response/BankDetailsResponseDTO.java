package com.orbit.payroll.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDetailsResponseDTO {
    private UUID id;
    private String accountName;
    private String bankName;
    private String accountNumber;
    private String ifsc;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
