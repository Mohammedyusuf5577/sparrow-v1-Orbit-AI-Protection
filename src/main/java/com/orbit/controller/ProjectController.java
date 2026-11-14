package com.orbit.controller;

import com.orbit.dto.request.ProjectCreateRequest;
import com.orbit.dto.request.ProjectUpdateRequest;
import com.orbit.dto.response.ApiResponse;
import com.orbit.dto.response.PageResponse;
import com.orbit.dto.response.ProjectDetailResponse;
import com.orbit.service.ProjectService;
import com.orbit.util.AppUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/projects")
@RequiredArgsConstructor
@Tag(name = "Project Management", description = "Admin Project Management APIs")
public class ProjectController {

    private final ProjectService projectService;
    private final com.orbit.service.ProjectStatsService projectStatsService;

    @PostMapping
    @Operation(summary = "Create a new project with team assignments and notification outbox")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> createProject(
            @Valid @RequestBody ProjectCreateRequest request
    ) {
        ProjectDetailResponse created = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(AppUtils.Status.PROJECT_CREATED.getMessage(), created)
        );
    }

    @GetMapping
    @Operation(summary = "Get paginated, filtered, and sorted projects")
    public ResponseEntity<ApiResponse<PageResponse<ProjectDetailResponse>>> getProjects(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AppUtils.ProjectStatus status,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder
    ) {
        PageResponse<ProjectDetailResponse> response = projectService.getProjects(
                page, limit, search, status, month, year, sortBy, sortOrder
        );
        return ResponseEntity.ok(
                ApiResponse.success(AppUtils.Status.PROJECTS_FETCHED.getMessage(), response)
        );
    }

    @GetMapping("/stats")
    @Operation(summary = "Get project statistics aggregated by month for a given year")
    public ResponseEntity<ApiResponse<com.orbit.dto.response.ProjectStatsResponse>> getProjectStats(
            @RequestParam(required = false) Integer year
    ) {
        com.orbit.dto.response.ProjectStatsResponse stats = projectStatsService.getProjectStats(year);
        return ResponseEntity.ok(
                ApiResponse.success(AppUtils.Status.PROJECT_STATS_FETCHED.getMessage(), stats)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project details by ID including assignees")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> getProjectById(
            @PathVariable UUID id
    ) {
        ProjectDetailResponse project = projectService.getProjectById(id);
        return ResponseEntity.ok(
                ApiResponse.success(AppUtils.Status.PROJECTS_FETCHED.getMessage(), project)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing project and sync team assignments")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectUpdateRequest request
    ) {
        ProjectDetailResponse updated = projectService.updateProject(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(AppUtils.Status.PROJECT_UPDATED.getMessage(), updated)
        );
    }

    @PatchMapping("/{id}/soft-delete")
    @Operation(summary = "Soft delete a project")
    public ResponseEntity<ApiResponse<Void>> softDeleteProject(
            @PathVariable UUID id
    ) {
        projectService.softDeleteProject(id);
        return ResponseEntity.ok(
                ApiResponse.success(AppUtils.Status.PROJECT_SOFT_DELETED.getMessage())
        );
    }
}
