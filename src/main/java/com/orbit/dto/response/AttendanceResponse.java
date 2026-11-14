package com.orbit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private UUID id;
    private UUID userId;
    private String employeeName;
    private LocalDate date;
    private String status;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
}

