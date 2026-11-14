package com.orbit.util;

import com.orbit.exception.AppException;
import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static java.lang.String.format;

public interface AppUtils {

    ModelMapper modelMapper = new ModelMapper();

    static <S, T> T map(S source, Class<T> targetClass) {
        try {
            modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            return modelMapper.map(source, targetClass);
        } catch (Exception e) {
            throw new AppException(Status.CONVERT_FAILED);
        }
    }

    static <S, T> void map(S source, T target) {
        try {
            modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            modelMapper.map(source, target);
        } catch (Exception e) {
            throw new AppException(Status.CONVERT_FAILED);
        }
    }

    static UUID getPrincipalUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            try {
                return UUID.fromString(userDetails.getUsername());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    // ---- User Roles ----
    enum UserRole {
        ADMIN, INSTRUCTOR, EMPLOYEE, BENCH
    }

    // ---- User Status ----
    enum UserStatus {
        ACTIVE, INACTIVE
    }

    // ---- Project Status ----
    enum ProjectStatus {
        COMPLETED, IN_PROGRESS, DELAYED
    }

    // ---- Task Status ----
    enum TaskStatus {
        PENDING, DONE
    }

    // ---- Attendance Status ----
    enum AttendanceStatus {
        PRESENT, ABSENT, LATE
    }

    // ---- Leave Status ----
    enum LeaveStatus {
        PENDING, APPROVED, REJECTED
    }

    // ---- Payment Status ----
    enum PaymentStatus {
        CREATED, SUCCESS, FAILED, REFUNDED
    }

    // ---- Payout Status ----
    enum PayoutStatus {
        PENDING, PROCESSING, PROCESSED, SUCCESS, FAILED, REVERSED, CANCELLED
    }

    // ---- Payout Mode ----
    enum PayoutMode {
        IMPS, NEFT, RTGS, UPI
    }

    // ---- Notification Type ----
    enum NotificationType {
        RISK_ALERT, INFO
    }

    // ---- Risk Level ----
    enum RiskLevel {
        LOW, MEDIUM, HIGH;

        public static RiskLevel fromScore(int score) {
            if (score <= 33)
                return LOW;
            if (score <= 66)
                return MEDIUM;
            return HIGH;
        }
    }

    // ---- Status Enum (Replaces Messages) ----
    @Getter
    enum Status {
        // App Core
        VALIDATION_FAILED("Validation failed", HttpStatus.BAD_REQUEST),
        INVALID_REQUEST_BODY("Invalid request body", HttpStatus.BAD_REQUEST),
        UNEXPECTED_ERROR("Unexpected error occurred: %s", HttpStatus.INTERNAL_SERVER_ERROR),
        CONVERT_FAILED("Failed to convert DTO/Entity", HttpStatus.INTERNAL_SERVER_ERROR),
        ACCESS_DENIED("Access denied", HttpStatus.FORBIDDEN),

        // Entity States & General Status
        ACTIVE("Active", HttpStatus.OK),
        INACTIVE("Inactive", HttpStatus.OK),

        // Auth
        AUTH_SUCCESS("Authentication successful", HttpStatus.OK),
        INVALID_CREDENTIALS("Invalid credentials", HttpStatus.UNAUTHORIZED),
        TOKEN_EXPIRED("Token has expired", HttpStatus.UNAUTHORIZED),
        ONLY_ADMIN_LOGIN("Only admin users can login through this portal", HttpStatus.FORBIDDEN),
        ACCOUNT_INACTIVE("Account is inactive", HttpStatus.FORBIDDEN),

        // User
        USER_NOT_FOUND("User not found", HttpStatus.NOT_FOUND),
        USER_CREATED("User created successfully", HttpStatus.CREATED),
        USER_UPDATED("User updated successfully", HttpStatus.OK),
        USER_DELETED("User deleted successfully", HttpStatus.OK),
        EMAIL_ALREADY_EXISTS("Email already exists", HttpStatus.BAD_REQUEST),
        EMPLOYEES_FETCHED("Employees fetched successfully", HttpStatus.OK),
        INSTRUCTORS_FETCHED("Instructors fetched successfully", HttpStatus.OK),
        EMPLOYEE_FETCHED("Employee details fetched successfully", HttpStatus.OK),
        SALARIES_FETCHED("Salaries fetched successfully", HttpStatus.OK),
        USER_NOT_INSTRUCTOR("User is not an instructor", HttpStatus.BAD_REQUEST),

        // Project
        PROJECT_NOT_FOUND("Project not found", HttpStatus.NOT_FOUND),
        PROJECT_CREATED("Project created successfully", HttpStatus.CREATED),
        PROJECTS_FETCHED("Projects fetched successfully", HttpStatus.OK),
        PROJECT_UPDATED("Project updated successfully", HttpStatus.OK),
        PROJECT_DELETED("Project deleted successfully", HttpStatus.OK),
        PROJECT_SOFT_DELETED("Project soft-deleted successfully", HttpStatus.OK),
        PROJECT_STATS_FETCHED("Project statistics fetched successfully", HttpStatus.OK),
        NOTIFICATION_OUTBOX_ENQUEUED("Notification outbox enqueued successfully", HttpStatus.OK),
        DASHBOARD_SUMMARY_FETCHED("Dashboard summary fetched successfully", HttpStatus.OK),
        INVALID_SORT_FIELD("Invalid sort field: %s", HttpStatus.BAD_REQUEST),
        INVALID_PAGE_LIMIT("Invalid page limit: %s. Allowed limits are 10, 25, 50, 100", HttpStatus.BAD_REQUEST),
        PROJECT_DATE_RANGE_INVALID("Start date must be before or equal to end date", HttpStatus.BAD_REQUEST),
        INSTRUCTOR_ASSIGNED("Instructor assigned to project successfully", HttpStatus.OK),
        INSTRUCTOR_ALREADY_ASSIGNED("Instructor already assigned to this project", HttpStatus.BAD_REQUEST),

        // Task
        TASK_NOT_FOUND("Task not found", HttpStatus.NOT_FOUND),

        // Leave
        LEAVE_NOT_FOUND("Leave request not found", HttpStatus.NOT_FOUND),
        LEAVES_FETCHED("Leave requests fetched successfully", HttpStatus.OK),
        LEAVE_APPROVED("Leave request approved", HttpStatus.OK),
        LEAVE_REJECTED("Leave request rejected", HttpStatus.OK),
        LEAVE_ALREADY_PROCESSED("Leave request already processed", HttpStatus.BAD_REQUEST),

        // Attendance
        ATTENDANCE_FETCHED("Attendance records fetched successfully", HttpStatus.OK),
        ATTENDANCE_NOT_FOUND("Attendance record not found", HttpStatus.NOT_FOUND),
        ATTENDANCE_UPDATED("Attendance record updated successfully", HttpStatus.OK),
        ATTENDANCE_DELETED("Attendance record deleted successfully", HttpStatus.OK),

        // Notifications
        NOTIFICATION_NOT_FOUND("Notification not found", HttpStatus.NOT_FOUND),
        NOTIFICATIONS_FETCHED("Notifications fetched successfully", HttpStatus.OK),
        NOTIFICATION_MARKED_READ("Notification marked as read", HttpStatus.OK),

        // AI & Dashboard
        AI_SUMMARY_FETCHED("AI summary fetched successfully", HttpStatus.OK),
        DASHBOARD_FETCHED("Dashboard data fetched successfully", HttpStatus.OK),

        // Payroll
        PAYROLL_NOT_FOUND("Employee payroll record not found", HttpStatus.NOT_FOUND),
        PAYROLL_CREATED("Employee payroll created successfully", HttpStatus.CREATED),
        PAYROLL_UPDATED("Employee payroll updated successfully", HttpStatus.OK),
        PAYROLL_DELETED("Employee payroll deleted successfully", HttpStatus.OK),
        PAYROLL_FETCHED("Payroll details fetched successfully", HttpStatus.OK),
        PAYROLL_LIST_FETCHED("Payroll list fetched successfully", HttpStatus.OK),
        BANK_DETAILS_SAVED("Bank details saved successfully", HttpStatus.OK),
        TAX_DETAILS_SAVED("Tax details saved successfully", HttpStatus.OK),
        SALARY_STRUCTURE_SAVED("Salary structure saved successfully", HttpStatus.OK),
        ATTENDANCE_SAVED("Attendance record saved successfully", HttpStatus.OK),

        // Payout
        PAYOUT_RECORD_NOT_FOUND("Payout record not found", HttpStatus.NOT_FOUND),
        PAYOUT_TRANSACTION_NOT_FOUND("Payout transaction not found", HttpStatus.NOT_FOUND),
        PAYOUT_RECORD_CREATED("Payout record created successfully", HttpStatus.CREATED),
        PAYOUT_RECORD_UPDATED("Payout record updated successfully", HttpStatus.OK),
        PAYOUT_RECORDS_FETCHED("Payout records fetched successfully", HttpStatus.OK),
        PAYOUT_TRANSACTIONS_FETCHED("Payout transactions fetched successfully", HttpStatus.OK),
        PAYOUT_INITIATED("Payout initiated successfully", HttpStatus.OK),
        PAYOUT_ALREADY_PROCESSED("Payout already processed for this record", HttpStatus.BAD_REQUEST),
        PAYOUT_FAILED("Payout failed: %s", HttpStatus.INTERNAL_SERVER_ERROR),
        PAYOUT_CONTACT_CREATED("Razorpay contact created: %s", HttpStatus.OK),
        PAYOUT_FUND_ACCOUNT_CREATED("Razorpay fund account created: %s", HttpStatus.OK),
        PAYOUT_ALL_TRIGGERED("Bulk salary payout triggered", HttpStatus.OK),
        PAYOUT_RETRY_TRIGGERED("Retry payout triggered for failed records", HttpStatus.OK),
        BANK_DETAILS_NOT_FOUND("Bank details not found for employee", HttpStatus.NOT_FOUND),
        BANK_DETAILS_REGISTERED("Bank details registered with Razorpay successfully", HttpStatus.OK),
        INSUFFICIENT_BALANCE("Insufficient balance in RazorpayX account", HttpStatus.BAD_REQUEST),
        WEBHOOK_SIGNATURE_INVALID("Webhook signature verification failed", HttpStatus.UNAUTHORIZED),

        // Payment
        PAYMENT_ORDER_CREATED("Payment order created successfully", HttpStatus.CREATED),
        PAYMENT_VERIFIED("Payment verified successfully", HttpStatus.OK),
        PAYMENT_FAILED("Payment verification failed", HttpStatus.BAD_REQUEST),
        PAYMENT_NOT_FOUND("Payment record not found", HttpStatus.NOT_FOUND),
        PAYMENT_ALREADY_PROCESSED("Payment already processed", HttpStatus.BAD_REQUEST),
        PAYMENTS_FETCHED("Payment history fetched successfully", HttpStatus.OK),
        REFUND_INITIATED("Refund initiated successfully", HttpStatus.OK),
        INVALID_SIGNATURE("Invalid Razorpay signature", HttpStatus.BAD_REQUEST),
        RAZORPAY_ERROR("Razorpay API error: %s", HttpStatus.INTERNAL_SERVER_ERROR),
        WEBHOOK_PROCESSED("Webhook processed successfully", HttpStatus.OK),

        // Logs
        USER_CREATED_LOG("User created: %s with role %s", HttpStatus.OK),
        USER_UPDATED_LOG("User updated: %s", HttpStatus.OK),
        USER_DELETED_LOG("User deleted: %s", HttpStatus.OK),
        PROJECT_CREATED_LOG("Project created: %s", HttpStatus.OK),
        PROJECT_UPDATED_LOG("Project updated: %s", HttpStatus.OK),
        PROJECT_DELETED_LOG("Project deleted: %s", HttpStatus.OK),
        INSTRUCTOR_ASSIGNED_LOG("Instructor %s assigned to project %s", HttpStatus.OK),
        LEAVE_APPROVED_LOG("Leave request approved for user: %s", HttpStatus.OK),
        LEAVE_REJECTED_LOG("Leave request rejected for user: %s", HttpStatus.OK),
        ADMIN_LOGIN_SUCCESS_LOG("Admin login successful for: %s", HttpStatus.OK),
        AI_MONITORING_STARTED("=== AI Monitoring Job Started ===", HttpStatus.OK),
        AI_MONITORING_COMPLETED("=== AI Monitoring Job Completed ===", HttpStatus.OK),
        PROJECT_MARKED_DELAYED("Project marked DELAYED: %s", HttpStatus.OK),
        AI_RISK_SCORE_LOG("AI Risk Score: %d (%s)", HttpStatus.OK),
        RISK_ALERT_MESSAGE(
                "⚠️ RISK ALERT: Score %d (%s) - %d delayed projects, %d pending leaves, %d attendance anomalies",
                HttpStatus.OK),
        RISK_ALERT_SENT_LOG("Risk alert sent to admin: %s", HttpStatus.OK);

        private final String message;
        private final HttpStatus httpStatus;

        Status(String message, HttpStatus httpStatus) {
            this.message = message;
            this.httpStatus = httpStatus;
        }

        public String getMessage(Object... args) {
            return format(message, args);
        }
    }

    class Constants {
        public static final String BEARER = "Bearer ";
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String SECURITY_SCHEME_NAME = "bearerAuth";

        public static final String STATUS_REQUIRED = "Status is required";
        public static final String EMAIL_REQUIRED = "Email is required";
        public static final String FULL_NAME_REQUIRED = "Full name is required";
        public static final String ROLE_REQUIRED = "Role is required";
        public static final String PROJECT_NAME_REQUIRED = "Project name is required";
        public static final String INVALID_EMAIL = "Invalid email format";

        private Constants() {
        }
    }
}
