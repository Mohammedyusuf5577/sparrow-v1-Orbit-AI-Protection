package com.orbit.service;

import com.orbit.util.AppUtils;

public interface AiRiskService {
    int calculateRiskScore(long totalProjects, long delayedProjects, long totalEmployees, long pendingLeaves, long absentToday);
    AppUtils.RiskLevel getRiskLevel(int score);
}
