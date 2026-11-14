package com.orbit.dto.request;

import com.orbit.util.AppUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {

    @NotBlank(message = AppUtils.Constants.PROJECT_NAME_REQUIRED)
    private String name;

    private String description;
    private String startDate;
    private String endDate;
    private AppUtils.ProjectStatus status;
}
