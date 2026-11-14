package com.orbit.dto.response;

import com.orbit.util.AppUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailResponse {
    private UUID id;
    private String name;
    private String clientName;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private AppUtils.ProjectStatus status;
    private int assignedUserCount;
    private List<AssignedUserSummary> assignees;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
