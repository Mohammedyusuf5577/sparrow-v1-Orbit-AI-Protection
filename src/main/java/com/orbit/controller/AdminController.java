package com.orbit.controller;

import com.orbit.dto.request.AssignInstructorRequest;
import com.orbit.dto.request.AttendanceRequest;
import com.orbit.dto.request.ProjectRequest;
import com.orbit.dto.request.UserRequest;
import com.orbit.dto.response.*;
import com.orbit.service.AdminService;
import com.orbit.util.AppUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin Module APIs")
public class AdminController {

    private final AdminService adminService;

    // ==================== Employee Monitoring ====================

    @GetMapping("/employees")
    @Operation(summary = "Get all employees (supports optional pagination and DB search)")
    public ResponseEntity<?> getAllEmployees(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String search
    ) {
        if (page != null || limit != null || search != null) {
            int p = page != null ? page : 1;
            int l = limit != null ? limit : 10;
            return ResponseEntity.ok(ApiResponse.success(
                    AppUtils.Status.EMPLOYEES_FETCHED.getMessage(),
                    adminService.getEmployeesPaginated(p, l, search)));
        }
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.EMPLOYEES_FETCHED.getMessage(), adminService.getAllEmployees()));
    }

    @GetMapping("/instructors")
    @Operation(summary = "Get all instructors")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllInstructors() {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.INSTRUCTORS_FETCHED.getMessage(), adminService.getAllInstructors()));
    }

    @GetMapping("/employees/{id}")
    @Operation(summary = "Get employee details")
    public ResponseEntity<ApiResponse<UserResponse>> getEmployeeById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.EMPLOYEE_FETCHED.getMessage(), adminService.getEmployeeById(id)));
    }

    @PostMapping("/employees")
    @Operation(summary = "Create a new employee/instructor")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.USER_CREATED.getMessage(), adminService.createUser(request)));
    }

    @PutMapping("/employees/{id}")
    @Operation(summary = "Update employee/instructor details")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable UUID id,
            @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.USER_UPDATED.getMessage(), adminService.updateUser(id, request)));
    }

    @DeleteMapping("/employees/{id}")
    @Operation(summary = "Delete employee/instructor")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(AppUtils.Status.USER_DELETED.getMessage()));
    }

    @GetMapping("/salaries")
    @Operation(summary = "Get all salary data")
    public ResponseEntity<ApiResponse<List<SalaryResponse>>> getAllSalaries() {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.SALARIES_FETCHED.getMessage(), adminService.getAllSalaries()));
    }


    // ==================== Attendance & Leave ====================

    @GetMapping("/attendance")
    @Operation(summary = "Get all attendance records")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAllAttendance() {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.ATTENDANCE_FETCHED.getMessage(), adminService.getAllAttendance()));
    }

    @PutMapping("/attendance/{id}")
    @Operation(summary = "Update an attendance record")
    public ResponseEntity<ApiResponse<AttendanceResponse>> updateAttendance(
            @PathVariable UUID id, @Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.ATTENDANCE_UPDATED.getMessage(), adminService.updateAttendance(id, request)));
    }

    @DeleteMapping("/attendance/{id}")
    @Operation(summary = "Delete an attendance record")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(@PathVariable UUID id) {
        adminService.deleteAttendance(id);
        return ResponseEntity.ok(ApiResponse.success(AppUtils.Status.ATTENDANCE_DELETED.getMessage()));
    }

    @GetMapping("/leaves")
    @Operation(summary = "Get all leave requests (optional status filter)")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getAllLeaves(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.LEAVES_FETCHED.getMessage(), adminService.getAllLeaves(status)));
    }

    @PutMapping("/leaves/{id}/approve")
    @Operation(summary = "Approve a leave request")
    public ResponseEntity<ApiResponse<LeaveResponse>> approveLeave(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.LEAVE_APPROVED.getMessage(), adminService.approveLeave(id)));
    }

    @PutMapping("/leaves/{id}/reject")
    @Operation(summary = "Reject a leave request")
    public ResponseEntity<ApiResponse<LeaveResponse>> rejectLeave(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.LEAVE_REJECTED.getMessage(), adminService.rejectLeave(id)));
    }

    // ==================== AI Monitoring ====================

    @GetMapping("/ai-summary")
    @Operation(summary = "Get AI monitoring summary with risk score")
    public ResponseEntity<ApiResponse<AiSummaryResponse>> getAiSummary() {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.AI_SUMMARY_FETCHED.getMessage(), adminService.getAiSummary()));
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard data")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.DASHBOARD_FETCHED.getMessage(), adminService.getDashboard()));
    }

    // ==================== Notifications ====================

    @GetMapping("/notifications")
    @Operation(summary = "Get all notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications() {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.NOTIFICATIONS_FETCHED.getMessage(), adminService.getNotifications()));
    }

    @PutMapping("/notifications/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<Void>> markNotificationRead(@PathVariable UUID id) {
        adminService.markNotificationRead(id);
        return ResponseEntity.ok(ApiResponse.success(AppUtils.Status.NOTIFICATION_MARKED_READ.getMessage()));
    }
}
