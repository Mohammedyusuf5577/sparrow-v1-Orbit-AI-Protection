package com.orbit.service;

import com.orbit.dto.request.*;
import com.orbit.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    // Employee Monitoring
    List<UserResponse> getAllEmployees();

    com.orbit.dto.response.PageResponse<UserResponse> getEmployeesPaginated(int page, int limit, String search);

    List<UserResponse> getAllInstructors();

    UserResponse getEmployeeById(UUID id);

    UserResponse createUser(UserRequest request);

    UserResponse updateUser(UUID id, UserRequest request);

    void deleteUser(UUID id);

    List<SalaryResponse> getAllSalaries();


    // Attendance & Leave
    List<AttendanceResponse> getAllAttendance();

    AttendanceResponse updateAttendance(UUID id, AttendanceRequest request);

    void deleteAttendance(UUID id);

    List<LeaveResponse> getAllLeaves(String status);

    LeaveResponse approveLeave(UUID id);

    LeaveResponse rejectLeave(UUID id);

    // AI Monitoring
    AiSummaryResponse getAiSummary();

    // Dashboard
    DashboardResponse getDashboard();

    // Notifications
    List<NotificationResponse> getNotifications();

    void markNotificationRead(UUID id);
}
