package com.orbit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatsResponse {
    private int year;
    private long totalProjects;
    private long totalCompleted;
    private long totalInProgress;
    private long totalDelayed;
    private List<ProjectMonthStatsResponse> monthlyBreakdown;
}
