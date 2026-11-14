package com.orbit.scheduler;

import com.orbit.entity.NotificationEntity;
import com.orbit.entity.ProjectEntity;
import com.orbit.repository.*;
import com.orbit.util.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiMonitoringScheduler {

    private final ProjectRepository projectRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Value("${app.ai.risk.threshold}")
    private int riskThreshold;

    @Scheduled(fixedRateString = "${app.ai.monitoring.rate}")
    @Transactional
    public void runAiMonitoring() {
        log.info(AppUtils.Status.AI_MONITORING_STARTED.getMessage());

        // Step 1: Detect and mark delayed projects
        List<ProjectEntity> delayedProjects = projectRepository.findDelayedProjects(
                LocalDateTime.now(), AppUtils.ProjectStatus.COMPLETED);
        for (ProjectEntity project : delayedProjects) {
            if (project.getStatus() != AppUtils.ProjectStatus.DELAYED) {
                project.setStatus(AppUtils.ProjectStatus.DELAYED);
                projectRepository.save(project);
                log.warn(AppUtils.Status.PROJECT_MARKED_DELAYED.getMessage(project.getName()));
            }
        }

        // Step 2: Count pending leaves
        long pendingLeaves = leaveRequestRepository.countByStatus(AppUtils.LeaveStatus.PENDING);

        // Step 3: Count attendance anomalies
        long attendanceAnomalies = attendanceRepository.countByStatus(AppUtils.AttendanceStatus.ABSENT)
                + attendanceRepository.countByStatus(AppUtils.AttendanceStatus.LATE);

        // Step 4: Calculate Risk Score
        int rawScore = (int) ((delayedProjects.size() * 10) + (pendingLeaves * 5) + (attendanceAnomalies * 5));
        int riskScore = Math.min(rawScore, 100);

        AppUtils.RiskLevel riskLevel = AppUtils.RiskLevel.fromScore(riskScore);

        log.info(AppUtils.Status.AI_RISK_SCORE_LOG.getMessage(riskScore, riskLevel.name()));

        // Step 5: Generate alert notification if risk > threshold
        if (riskScore > riskThreshold) {
            String message = AppUtils.Status.RISK_ALERT_MESSAGE.getMessage(
                    riskScore, riskLevel.name(), delayedProjects.size(), pendingLeaves, attendanceAnomalies);

            // Find all admin users and send notification
            userRepository.findByRole(AppUtils.UserRole.ADMIN).forEach(admin -> {
                NotificationEntity notification = NotificationEntity.builder()
                        .user(admin)
                        .message(message)
                        .type(AppUtils.NotificationType.RISK_ALERT)
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);
                log.info(AppUtils.Status.RISK_ALERT_SENT_LOG.getMessage(admin.getEmail()));
            });
        }

        log.info(AppUtils.Status.AI_MONITORING_COMPLETED.getMessage());
    }
}

