package com.orbit.dto.request;

import com.orbit.util.AppUtils;
import com.orbit.validation.HasDateRange;
import com.orbit.validation.ValidDateRange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@ValidDateRange
public class ProjectUpdateRequest implements HasDateRange {

    @NotBlank(message = AppUtils.Constants.PROJECT_NAME_REQUIRED)
    private String name;

    @NotBlank(message = "Client name is required")
    private String clientName;

    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @NotNull(message = AppUtils.Constants.STATUS_REQUIRED)
    private AppUtils.ProjectStatus status;

    private List<UUID> assigneeIds;
}
