package com.orbit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMonthStatsResponse {
    private int month; // 1-12
    private String monthName; // "Jan", "Feb", ...
    private long total;
    private long completed;
    private long inProgress;
    private long delayed;
}
