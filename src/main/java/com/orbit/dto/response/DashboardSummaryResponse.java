package com.orbit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    // Project status breakdown
    private long totalProjects;
    private long inProgressProjects;
    private long delayedProjects;
    private long completedProjects;

    // Team stats
    private long totalEmployees;
    private long activeEmployees;
    private long totalInstructors;
    private long activeInstructors;

    // Attendance stats for today
    private long presentToday;
    private long absentToday;
    private long lateToday;

    // Leaves
    private long pendingLeaves;

    // AI Risk
    private int riskScore;
    private String riskLevel;
    private String riskAssessment;
}
