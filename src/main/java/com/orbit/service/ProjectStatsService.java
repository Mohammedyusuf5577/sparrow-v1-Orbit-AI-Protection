package com.orbit.service;

import com.orbit.dto.response.ProjectStatsResponse;

public interface ProjectStatsService {
    ProjectStatsResponse getProjectStats(Integer year);
}
