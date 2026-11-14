package com.orbit.controller;

import com.orbit.dto.response.ApiResponse;
import com.orbit.dto.response.DashboardSummaryResponse;
import com.orbit.service.DashboardService;
import com.orbit.util.AppUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Admin Dashboard Analytics APIs")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get high-performance consolidated admin dashboard summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(
                ApiResponse.success(AppUtils.Status.DASHBOARD_SUMMARY_FETCHED.getMessage(), summary)
        );
    }
}
