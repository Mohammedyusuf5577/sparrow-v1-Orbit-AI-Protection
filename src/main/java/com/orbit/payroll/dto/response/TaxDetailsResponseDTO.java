package com.orbit.payroll.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxDetailsResponseDTO {
    private UUID id;
    private String pan;
    private String aadhaar;
    private String uan;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
