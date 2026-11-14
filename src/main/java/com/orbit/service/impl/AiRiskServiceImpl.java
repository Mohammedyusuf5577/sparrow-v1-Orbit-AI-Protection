package com.orbit.service.impl;

import com.orbit.service.AiRiskService;
import com.orbit.util.AppUtils;
import org.springframework.stereotype.Service;

@Service
public class AiRiskServiceImpl implements AiRiskService {

    @Override
    public int calculateRiskScore(long totalProjects, long delayedProjects, long totalEmployees, long pendingLeaves, long absentToday) {
        double delayedRatio = totalProjects > 0 ? (double) delayedProjects / totalProjects : 0.0;
        double leaveRatio = totalEmployees > 0 ? Math.min(1.0, (double) pendingLeaves / totalEmployees) : 0.0;
        double absenceRatio = totalEmployees > 0 ? Math.min(1.0, (double) absentToday / totalEmployees) : 0.0;

        // Weighted risk score: 40% project delay + 30% leave overload + 30% absenteeism
        double rawScore = (delayedRatio * 40.0) + (leaveRatio * 30.0) + (absenceRatio * 30.0);
        return Math.max(0, Math.min(100, (int) Math.round(rawScore)));
    }

    @Override
    public AppUtils.RiskLevel getRiskLevel(int score) {
        return AppUtils.RiskLevel.fromScore(score);
    }
}
