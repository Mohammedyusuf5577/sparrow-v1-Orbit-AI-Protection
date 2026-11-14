package com.orbit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSummaryResponse {
    private long activeEmployees;
    private long totalProjects;
    private long delayedProjects;
    private long pendingLeaves;
    private long attendanceAnomalies;
    private int riskScore;
    private String riskLevel;
    private long completedProjects;
    private long inProgressProjects;
}

