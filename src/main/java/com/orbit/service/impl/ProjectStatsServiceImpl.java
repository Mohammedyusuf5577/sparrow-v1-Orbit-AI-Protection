package com.orbit.service.impl;

import com.orbit.dto.response.ProjectMonthStatsResponse;
import com.orbit.dto.response.ProjectStatsResponse;
import com.orbit.repository.ProjectRepository;
import com.orbit.service.ProjectStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectStatsServiceImpl implements ProjectStatsService {

    private final ProjectRepository projectRepository;

    @Override
    @Transactional(readOnly = true)
    public ProjectStatsResponse getProjectStats(Integer year) {
        int targetYear = (year != null && year >= 2000 && year <= 2100) ? year : LocalDate.now().getYear();

        // 1 SINGLE SQL query with GROUP BY EXTRACT(MONTH FROM start_date)
        List<Object[]> rows = projectRepository.getMonthlyStatsNative(targetYear);

        Map<Integer, ProjectMonthStatsResponse> monthMap = new HashMap<>();
        for (Object[] row : rows) {
            if (row[0] != null) {
                int m = ((Number) row[0]).intValue();
                long total = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                long completed = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                long inProgress = row[3] != null ? ((Number) row[3]).longValue() : 0L;
                long delayed = row[4] != null ? ((Number) row[4]).longValue() : 0L;

                monthMap.put(m, ProjectMonthStatsResponse.builder()
                        .month(m)
                        .monthName(Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                        .total(total)
                        .completed(completed)
                        .inProgress(inProgress)
                        .delayed(delayed)
                        .build());
            }
        }

        // Fill all 12 months in Java — zero extra DB queries
        List<ProjectMonthStatsResponse> monthlyBreakdown = new ArrayList<>(12);
        long yearTotal = 0L;
        long yearCompleted = 0L;
        long yearInProgress = 0L;
        long yearDelayed = 0L;

        for (int m = 1; m <= 12; m++) {
            ProjectMonthStatsResponse stats = monthMap.get(m);
            if (stats == null) {
                stats = ProjectMonthStatsResponse.builder()
                        .month(m)
                        .monthName(Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                        .total(0L)
                        .completed(0L)
                        .inProgress(0L)
                        .delayed(0L)
                        .build();
            }
            monthlyBreakdown.add(stats);
            yearTotal += stats.getTotal();
            yearCompleted += stats.getCompleted();
            yearInProgress += stats.getInProgress();
            yearDelayed += stats.getDelayed();
        }

        return ProjectStatsResponse.builder()
                .year(targetYear)
                .totalProjects(yearTotal)
                .totalCompleted(yearCompleted)
                .totalInProgress(yearInProgress)
                .totalDelayed(yearDelayed)
                .monthlyBreakdown(monthlyBreakdown)
                .build();
    }
}
