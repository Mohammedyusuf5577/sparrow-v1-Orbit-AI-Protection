package com.orbit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalEmployees;
    private long totalInstructors;
    private long totalProjects;
    private long activeProjects;
    private long delayedProjects;
    private long completedProjects;
    private long pendingLeaves;
    private int riskScore;
    private String riskLevel;
    private long presentToday;
    private long absentToday;
}

