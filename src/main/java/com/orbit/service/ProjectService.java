package com.orbit.service;

import com.orbit.dto.request.ProjectCreateRequest;
import com.orbit.dto.request.ProjectUpdateRequest;
import com.orbit.dto.response.PageResponse;
import com.orbit.dto.response.ProjectDetailResponse;
import com.orbit.util.AppUtils;

import java.util.UUID;

public interface ProjectService {

    PageResponse<ProjectDetailResponse> getProjects(
            int page,
            int limit,
            String search,
            AppUtils.ProjectStatus status,
            Integer month,
            Integer year,
            String sortBy,
            String sortOrder
    );

    ProjectDetailResponse getProjectById(UUID id);

    ProjectDetailResponse createProject(ProjectCreateRequest request);

    ProjectDetailResponse updateProject(UUID id, ProjectUpdateRequest request);

    void softDeleteProject(UUID id);
}
