package com.orbit.service.impl;

import com.orbit.dto.response.DashboardSummaryResponse;
import com.orbit.repository.AttendanceRepository;
import com.orbit.repository.LeaveRequestRepository;
import com.orbit.repository.ProjectRepository;
import com.orbit.repository.UserRepository;
import com.orbit.service.AiRiskService;
import com.orbit.service.DashboardService;
import com.orbit.util.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AiRiskService aiRiskService;

    @Qualifier("dashboardExecutor")
    private final Executor dashboardExecutor;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        LocalDate today = LocalDate.now();

        // 1. Project stats future (single query with CASE WHEN)
        CompletableFuture<long[]> projectStatsFuture = CompletableFuture.supplyAsync(() -> {
            List<Object[]> rows = projectRepository.getProjectStatusCounts();
            if (rows != null && !rows.isEmpty() && rows.get(0) != null) {
                Object[] r = rows.get(0);
                long total = r[0] != null ? ((Number) r[0]).longValue() : 0L;
                long completed = r[1] != null ? ((Number) r[1]).longValue() : 0L;
                long inProgress = r[2] != null ? ((Number) r[2]).longValue() : 0L;
                long delayed = r[3] != null ? ((Number) r[3]).longValue() : 0L;
                return new long[]{total, inProgress, delayed, completed};
            }
            return new long[]{0L, 0L, 0L, 0L};
        }, dashboardExecutor);

        // 2. Employee stats future
        CompletableFuture<long[]> employeeStatsFuture = CompletableFuture.supplyAsync(() -> {
            long total = userRepository.countByRole(AppUtils.UserRole.EMPLOYEE);
            long active = userRepository.countByRoleAndStatus(AppUtils.UserRole.EMPLOYEE, AppUtils.UserStatus.ACTIVE);
            return new long[]{total, active};
        }, dashboardExecutor);

        // 3. Instructor stats future
        CompletableFuture<long[]> instructorStatsFuture = CompletableFuture.supplyAsync(() -> {
            long total = userRepository.countByRole(AppUtils.UserRole.INSTRUCTOR);
            long active = userRepository.countByRoleAndStatus(AppUtils.UserRole.INSTRUCTOR, AppUtils.UserStatus.ACTIVE);
            return new long[]{total, active};
        }, dashboardExecutor);

        // 4. Attendance today stats future
        CompletableFuture<long[]> attendanceStatsFuture = CompletableFuture.supplyAsync(() -> {
            long present = attendanceRepository.countByDateAndStatus(today, AppUtils.AttendanceStatus.PRESENT);
            long absent = attendanceRepository.countByDateAndStatus(today, AppUtils.AttendanceStatus.ABSENT);
            long late = attendanceRepository.countByDateAndStatus(today, AppUtils.AttendanceStatus.LATE);
            return new long[]{present, absent, late};
        }, dashboardExecutor);

        // 5. Leave stats future
        CompletableFuture<Long> pendingLeavesFuture = CompletableFuture.supplyAsync(
                () -> leaveRequestRepository.countByStatus(AppUtils.LeaveStatus.PENDING),
                dashboardExecutor
        );

        // Join all in parallel: wall-clock time = max(individual queries)
        CompletableFuture.allOf(
                projectStatsFuture,
                employeeStatsFuture,
                instructorStatsFuture,
                attendanceStatsFuture,
                pendingLeavesFuture
        ).join();

        long[] projectCounts = projectStatsFuture.join();
        long[] employeeCounts = employeeStatsFuture.join();
        long[] instructorCounts = instructorStatsFuture.join();
        long[] attendanceCounts = attendanceStatsFuture.join();
        long pendingLeaves = pendingLeavesFuture.join();

        long totalProjects = projectCounts[0];
        long inProgressProjects = projectCounts[1];
        long delayedProjects = projectCounts[2];
        long completedProjects = projectCounts[3];

        long totalEmployees = employeeCounts[0];
        long activeEmployees = employeeCounts[1];
        long totalInstructors = instructorCounts[0];
        long activeInstructors = instructorCounts[1];

        long presentToday = attendanceCounts[0];
        long absentToday = attendanceCounts[1];
        long lateToday = attendanceCounts[2];

        int riskScore = aiRiskService.calculateRiskScore(
                totalProjects, delayedProjects, totalEmployees, pendingLeaves, absentToday
        );
        AppUtils.RiskLevel riskLevel = aiRiskService.getRiskLevel(riskScore);

        String assessment;
        if (riskScore <= 33) {
            assessment = "Operations are running normally with low organizational risk.";
        } else if (riskScore <= 66) {
            assessment = String.format("Moderate risk detected. %d delayed project(s) and %d pending leave(s) require attention.",
                    delayedProjects, pendingLeaves);
        } else {
            assessment = String.format("High risk alert! Significant project delays (%d) and workforce disruption detected.",
                    delayedProjects);
        }

        return DashboardSummaryResponse.builder()
                .totalProjects(totalProjects)
                .inProgressProjects(inProgressProjects)
                .delayedProjects(delayedProjects)
                .completedProjects(completedProjects)
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .totalInstructors(totalInstructors)
                .activeInstructors(activeInstructors)
                .presentToday(presentToday)
                .absentToday(absentToday)
                .lateToday(lateToday)
                .pendingLeaves(pendingLeaves)
                .riskScore(riskScore)
                .riskLevel(riskLevel.name())
                .riskAssessment(assessment)
                .build();
    }
}
