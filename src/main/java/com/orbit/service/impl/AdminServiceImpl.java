package com.orbit.service.impl;

import com.orbit.exception.AppException;

import com.orbit.dto.request.AssignInstructorRequest;
import com.orbit.dto.request.AttendanceRequest;
import com.orbit.dto.request.ProjectRequest;
import com.orbit.dto.request.UserRequest;
import com.orbit.dto.response.*;
import com.orbit.entity.*;
import com.orbit.repository.*;
import com.orbit.service.AdminService;
import com.orbit.util.AppUtils;
import com.orbit.payroll.entity.*;
import com.orbit.payroll.repository.*;
import com.orbit.payout.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final SalaryRepository salaryRepository;
    private final NotificationRepository notificationRepository;
    private final EmployeePayrollRepository employeePayrollRepository;
    private final PayrollBankDetailsRepository bankDetailsRepository;
    private final PayrollTaxDetailsRepository taxDetailsRepository;
    private final PayrollSalaryStructureRepository salaryStructureRepository;
    private final EncryptionUtil encryptionUtil;

    // ==================== Employee Monitoring ====================

    @Override
    public List<UserResponse> getAllEmployees() {
        return userRepository.findByRole(AppUtils.UserRole.EMPLOYEE)
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public com.orbit.dto.response.PageResponse<UserResponse> getEmployeesPaginated(int page, int limit, String search) {
        int validPage = Math.max(1, page);
        int validLimit = (limit > 0 && limit <= 100) ? limit : 10;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                validPage - 1, validLimit, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "fullName"));

        org.springframework.data.jpa.domain.Specification<UserEntity> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.equal(root.get("status"), AppUtils.UserStatus.ACTIVE));
            if (org.springframework.util.StringUtils.hasText(search)) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), term),
                        cb.like(cb.lower(root.get("email")), term)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        org.springframework.data.domain.Page<UserEntity> userPage = userRepository.findAll(spec, pageable);
        List<UserResponse> dtos = userPage.getContent().stream()
                .map(this::mapToUserResponse)
                .toList();

        return com.orbit.dto.response.PageResponse.of(dtos, validPage, validLimit, userPage.getTotalElements());
    }

    @Override
    public List<UserResponse> getAllInstructors() {
        return userRepository.findByRole(AppUtils.UserRole.INSTRUCTOR)
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getEmployeeById(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(AppUtils.Status.USER_NOT_FOUND));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(AppUtils.Status.EMAIL_ALREADY_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();
        String phoneVal = (request.getPhone() != null && !request.getPhone().trim().isEmpty())
                ? request.getPhone().trim()
                : "+91 98765 43210";

        UserEntity user = UserEntity.builder()
                .email(request.getEmail().trim().toLowerCase())
                .fullName(request.getFullName().trim())
                .phone(phoneVal)
                .password(request.getPassword() != null && !request.getPassword().trim().isEmpty() 
                        ? request.getPassword().trim() 
                        : "Orbit@123")
                .role(request.getRole())
                .status(request.getStatus())
                .lastLoginAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        user = userRepository.saveAndFlush(user);
        log.info(AppUtils.Status.USER_CREATED_LOG.getMessage(user.getFullName(), user.getRole()));

        saveOrUpdatePayrollAndBank(user, request);

        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UserRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(AppUtils.Status.USER_NOT_FOUND));

        if (request.getEmail() != null)
            user.setEmail(request.getEmail().trim().toLowerCase());
        if (request.getFullName() != null)
            user.setFullName(request.getFullName().trim());
        if (request.getRole() != null)
            user.setRole(request.getRole());
        if (request.getStatus() != null)
            user.setStatus(request.getStatus());
        if (request.getPhone() != null)
            user.setPhone(request.getPhone().trim());
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty())
            user.setPassword(request.getPassword().trim());
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.saveAndFlush(user);
        log.info(AppUtils.Status.USER_UPDATED_LOG.getMessage(user.getFullName()));

        saveOrUpdatePayrollAndBank(user, request);

        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new AppException(AppUtils.Status.USER_NOT_FOUND);
        }
        userRepository.deleteById(id);
        log.info(AppUtils.Status.USER_DELETED_LOG.getMessage(id));
    }

    @Override
    public List<SalaryResponse> getAllSalaries() {
        return salaryRepository.findAllByOrderByPaymentDateDesc()
                .stream()
                .map(this::mapToSalaryResponse)
                .collect(Collectors.toList());
    }


    // ==================== Attendance & Leave ====================

    @Override
    public List<AttendanceResponse> getAllAttendance() {
        return attendanceRepository.findAllByOrderByDateDesc()
                .stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttendanceResponse updateAttendance(UUID id, AttendanceRequest request) {
        AttendanceEntity attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new AppException(AppUtils.Status.ATTENDANCE_NOT_FOUND));

        attendance.setStatus(request.getStatus());
        if (request.getCheckInTime() != null) {
            attendance.setCheckInTime(request.getCheckInTime());
        }
        if (request.getCheckOutTime() != null) {
            attendance.setCheckOutTime(request.getCheckOutTime());
        }

        attendance = attendanceRepository.save(attendance);
        log.info(AppUtils.Status.ATTENDANCE_UPDATED.getMessage());
        return mapToAttendanceResponse(attendance);
    }

    @Override
    @Transactional
    public void deleteAttendance(UUID id) {
        if (!attendanceRepository.existsById(id)) {
            throw new AppException(AppUtils.Status.ATTENDANCE_NOT_FOUND);
        }
        attendanceRepository.deleteById(id);
        log.info(AppUtils.Status.ATTENDANCE_DELETED.getMessage());
    }

    @Override
    public List<LeaveResponse> getAllLeaves(String status) {
        List<LeaveRequestEntity> leaves;
        if (status != null && !status.isEmpty()) {
            leaves = leaveRequestRepository.findByStatus(AppUtils.LeaveStatus.valueOf(status.toUpperCase()));
        } else {
            leaves = leaveRequestRepository.findAllByOrderByCreatedAtDesc();
        }
        return leaves.stream()
                .map(this::mapToLeaveResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeaveResponse approveLeave(UUID id) {
        LeaveRequestEntity leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(AppUtils.Status.LEAVE_NOT_FOUND));

        if (leave.getStatus() != AppUtils.LeaveStatus.PENDING) {
            throw new AppException(AppUtils.Status.LEAVE_ALREADY_PROCESSED);
        }

        leave.setStatus(AppUtils.LeaveStatus.APPROVED);
        leave = leaveRequestRepository.save(leave);
        log.info(AppUtils.Status.LEAVE_APPROVED_LOG.getMessage(leave.getUser().getFullName()));
        return mapToLeaveResponse(leave);
    }

    @Override
    @Transactional
    public LeaveResponse rejectLeave(UUID id) {
        LeaveRequestEntity leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(AppUtils.Status.LEAVE_NOT_FOUND));

        if (leave.getStatus() != AppUtils.LeaveStatus.PENDING) {
            throw new AppException(AppUtils.Status.LEAVE_ALREADY_PROCESSED);
        }

        leave.setStatus(AppUtils.LeaveStatus.REJECTED);
        leave = leaveRequestRepository.save(leave);
        log.info(AppUtils.Status.LEAVE_REJECTED_LOG.getMessage(leave.getUser().getFullName()));
        return mapToLeaveResponse(leave);
    }

    // ==================== AI Monitoring ====================

    @Override
    public AiSummaryResponse getAiSummary() {
        long activeEmployees = userRepository.countByRoleAndStatus(AppUtils.UserRole.EMPLOYEE,
                AppUtils.UserStatus.ACTIVE);
        long totalProjects = projectRepository.count();
        long delayedProjects = projectRepository.countByStatus(AppUtils.ProjectStatus.DELAYED);
        long completedProjects = projectRepository.countByStatus(AppUtils.ProjectStatus.COMPLETED);
        long inProgressProjects = projectRepository.countByStatus(AppUtils.ProjectStatus.IN_PROGRESS);
        long pendingLeaves = leaveRequestRepository.countByStatus(AppUtils.LeaveStatus.PENDING);

        long attendanceAnomalies = attendanceRepository.countByStatus(AppUtils.AttendanceStatus.ABSENT)
                + attendanceRepository.countByStatus(AppUtils.AttendanceStatus.LATE);

        int riskScore = Math.min((int) ((delayedProjects * 10) + (pendingLeaves * 5) + (attendanceAnomalies * 5)), 100);
        String riskLevel = AppUtils.RiskLevel.fromScore(riskScore).name();

        return AiSummaryResponse.builder()
                .activeEmployees(activeEmployees)
                .totalProjects(totalProjects)
                .delayedProjects(delayedProjects)
                .pendingLeaves(pendingLeaves)
                .attendanceAnomalies(attendanceAnomalies)
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .completedProjects(completedProjects)
                .inProgressProjects(inProgressProjects)
                .build();
    }

    // ==================== Dashboard ====================

    @Override
    public DashboardResponse getDashboard() {
        long totalEmployees = userRepository.countByRole(AppUtils.UserRole.EMPLOYEE);
        long totalInstructors = userRepository.countByRole(AppUtils.UserRole.INSTRUCTOR);
        long totalProjects = projectRepository.count();
        long activeProjects = projectRepository.countByStatus(AppUtils.ProjectStatus.IN_PROGRESS);
        long delayedProjects = projectRepository.countByStatus(AppUtils.ProjectStatus.DELAYED);
        long completedProjects = projectRepository.countByStatus(AppUtils.ProjectStatus.COMPLETED);
        long pendingLeaves = leaveRequestRepository.countByStatus(AppUtils.LeaveStatus.PENDING);

        long attendanceAnomalies = attendanceRepository.countByStatus(AppUtils.AttendanceStatus.ABSENT)
                + attendanceRepository.countByStatus(AppUtils.AttendanceStatus.LATE);

        int riskScore = Math.min((int) ((delayedProjects * 10) + (pendingLeaves * 5) + (attendanceAnomalies * 5)), 100);
        String riskLevel = AppUtils.RiskLevel.fromScore(riskScore).name();

        LocalDate today = LocalDate.now();
        long presentToday = attendanceRepository.countByDateAndStatus(today, AppUtils.AttendanceStatus.PRESENT);
        long absentToday = attendanceRepository.countByDateAndStatus(today, AppUtils.AttendanceStatus.ABSENT);

        return DashboardResponse.builder()
                .totalEmployees(totalEmployees)
                .totalInstructors(totalInstructors)
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .delayedProjects(delayedProjects)
                .completedProjects(completedProjects)
                .pendingLeaves(pendingLeaves)
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .presentToday(presentToday)
                .absentToday(absentToday)
                .build();
    }

    // ==================== Notifications ====================

    @Override
    public List<NotificationResponse> getNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markNotificationRead(UUID id) {
        NotificationEntity notification = notificationRepository.findById(id)
                .orElseThrow(() -> new AppException(AppUtils.Status.NOTIFICATION_NOT_FOUND));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private UserResponse mapToUserResponse(UserEntity entity) {
        LocalDateTime now = LocalDateTime.now();
        String phoneVal = entity.getPhone() != null && !entity.getPhone().trim().isEmpty() 
                ? entity.getPhone() 
                : "+91 98765 43210";
        LocalDateTime createdVal = entity.getCreatedAt() != null ? entity.getCreatedAt() : now;
        LocalDateTime updatedVal = entity.getUpdatedAt() != null ? entity.getUpdatedAt() : now;
        LocalDateTime lastLoginVal = entity.getLastLoginAt() != null ? entity.getLastLoginAt() : createdVal;

        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .role(entity.getRole() != null ? entity.getRole().name() : "EMPLOYEE")
                .status(entity.getStatus() != null ? entity.getStatus().name() : "ACTIVE")
                .phone(phoneVal)
                .lastLoginAt(lastLoginVal)
                .createdAt(createdVal)
                .updatedAt(updatedVal);

        employeePayrollRepository.findByEmployeeId(entity.getId()).ifPresent(payroll -> {
            builder.department(payroll.getDepartment());
            builder.designation(payroll.getDesignation());
            builder.employmentType(payroll.getEmploymentType());
            builder.joinDate(payroll.getJoinDate() != null ? payroll.getJoinDate().toString() : null);

            PayrollBankDetailsEntity bank = payroll.getBankDetails();
            if (bank != null) {
                builder.accountHolderName(bank.getAccountName());
                builder.bankName(bank.getBankName());
                if (bank.getAccountNumber() != null) {
                    try {
                        String decrypted = encryptionUtil.decrypt(bank.getAccountNumber());
                        builder.accountNumber(maskAccountNumber(decrypted));
                    } catch (Exception e) {
                        builder.accountNumber(maskAccountNumber(bank.getAccountNumber()));
                    }
                }
                builder.ifsc(bank.getIfsc());
                builder.upiId(bank.getUpiId());
            }

            PayrollTaxDetailsEntity tax = payroll.getTaxDetails();
            if (tax != null) {
                builder.pan(maskPan(tax.getPan()));
                builder.aadhaar(maskAadhaar(tax.getAadhaar()));
                builder.uan(tax.getUan());
            }

            PayrollSalaryStructureEntity salary = payroll.getSalaryStructure();
            if (salary != null) {
                builder.basicSalary(salary.getBasicSalary());
                builder.allowance(salary.getAllowance());
            }
        });

        return builder.build();
    }

    private void saveOrUpdatePayrollAndBank(UserEntity user, UserRequest request) {
        boolean hasPayrollData = StringUtils.hasText(request.getDepartment())
                || StringUtils.hasText(request.getDesignation())
                || StringUtils.hasText(request.getEmploymentType())
                || StringUtils.hasText(request.getAccountNumber())
                || StringUtils.hasText(request.getBankName())
                || StringUtils.hasText(request.getIfsc())
                || StringUtils.hasText(request.getPan())
                || request.getBasicSalary() != null;

        if (!hasPayrollData) {
            return;
        }

        EmployeePayrollEntity payroll = employeePayrollRepository.findByEmployeeId(user.getId())
                .orElseGet(() -> {
                    LocalDate joinDate = LocalDate.now();
                    if (StringUtils.hasText(request.getJoinDate())) {
                        try {
                            joinDate = LocalDate.parse(request.getJoinDate().trim());
                        } catch (Exception ignored) {
                        }
                    }
                    return EmployeePayrollEntity.builder()
                            .employee(user)
                            .name(user.getFullName())
                            .email(user.getEmail())
                            .mobile(user.getPhone())
                            .department(StringUtils.hasText(request.getDepartment()) ? request.getDepartment().trim() : "General")
                            .designation(StringUtils.hasText(request.getDesignation()) ? request.getDesignation().trim() : "Staff")
                            .employmentType(StringUtils.hasText(request.getEmploymentType()) ? request.getEmploymentType().trim() : "FULL_TIME")
                            .joinDate(joinDate)
                            .build();
                });

        if (StringUtils.hasText(request.getDepartment()))
            payroll.setDepartment(request.getDepartment().trim());
        if (StringUtils.hasText(request.getDesignation()))
            payroll.setDesignation(request.getDesignation().trim());
        if (StringUtils.hasText(request.getEmploymentType()))
            payroll.setEmploymentType(request.getEmploymentType().trim());
        if (StringUtils.hasText(request.getJoinDate())) {
            try {
                payroll.setJoinDate(LocalDate.parse(request.getJoinDate().trim()));
            } catch (Exception ignored) {
            }
        }
        payroll.setName(user.getFullName());
        payroll.setEmail(user.getEmail());
        payroll.setMobile(user.getPhone());
        payroll = employeePayrollRepository.saveAndFlush(payroll);

        // Save Bank Details (Account number encrypted with AES)
        if (StringUtils.hasText(request.getAccountNumber())) {
            PayrollBankDetailsEntity bankDetails = payroll.getBankDetails();
            if (bankDetails == null) {
                bankDetails = PayrollBankDetailsEntity.builder()
                        .employeePayroll(payroll)
                        .retryCount(0)
                        .build();
            }
            String accountName = StringUtils.hasText(request.getAccountHolderName())
                    ? request.getAccountHolderName().trim()
                    : user.getFullName();
            bankDetails.setAccountName(accountName);
            bankDetails.setBankName(StringUtils.hasText(request.getBankName()) ? request.getBankName().trim() : "Bank");
            bankDetails.setAccountNumber(encryptionUtil.encrypt(request.getAccountNumber().trim()));
            if (StringUtils.hasText(request.getIfsc())) {
                bankDetails.setIfsc(request.getIfsc().trim().toUpperCase());
            }
            if (StringUtils.hasText(request.getUpiId())) {
                bankDetails.setUpiId(request.getUpiId().trim());
            }
            bankDetailsRepository.saveAndFlush(bankDetails);
            payroll.setBankDetails(bankDetails);
        }

        // Save Tax Details
        if (StringUtils.hasText(request.getPan()) || StringUtils.hasText(request.getAadhaar()) || StringUtils.hasText(request.getUan())) {
            PayrollTaxDetailsEntity taxDetails = payroll.getTaxDetails();
            if (taxDetails == null) {
                taxDetails = PayrollTaxDetailsEntity.builder()
                        .employeePayroll(payroll)
                        .build();
            }
            if (StringUtils.hasText(request.getPan())) {
                taxDetails.setPan(request.getPan().trim().toUpperCase());
            }
            if (StringUtils.hasText(request.getAadhaar())) {
                taxDetails.setAadhaar(request.getAadhaar().trim());
            }
            if (StringUtils.hasText(request.getUan())) {
                taxDetails.setUan(request.getUan().trim());
            }
            taxDetailsRepository.saveAndFlush(taxDetails);
            payroll.setTaxDetails(taxDetails);
        }

        // Save Salary Structure
        if (request.getBasicSalary() != null || request.getAllowance() != null) {
            PayrollSalaryStructureEntity salaryStructure = payroll.getSalaryStructure();
            if (salaryStructure == null) {
                salaryStructure = PayrollSalaryStructureEntity.builder()
                        .employeePayroll(payroll)
                        .pf(java.math.BigDecimal.ZERO)
                        .tax(java.math.BigDecimal.ZERO)
                        .otherDeduction(java.math.BigDecimal.ZERO)
                        .build();
            }
            if (request.getBasicSalary() != null) {
                salaryStructure.setBasicSalary(request.getBasicSalary());
            }
            if (request.getAllowance() != null) {
                salaryStructure.setAllowance(request.getAllowance());
            }
            salaryStructureRepository.saveAndFlush(salaryStructure);
            payroll.setSalaryStructure(salaryStructure);
        }
    }

    private String maskAccountNumber(String raw) {
        if (raw == null || raw.length() <= 4) return raw;
        return "••••••••" + raw.substring(raw.length() - 4);
    }

    private String maskPan(String raw) {
        if (raw == null || raw.length() <= 4) return raw;
        return "••••••" + raw.substring(raw.length() - 4);
    }

    private String maskAadhaar(String raw) {
        if (raw == null || raw.length() <= 4) return raw;
        return "••••••••" + raw.substring(raw.length() - 4);
    }

    private AttendanceResponse mapToAttendanceResponse(AttendanceEntity entity) {
        return AttendanceResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .employeeName(entity.getUser().getFullName())
                .date(entity.getDate())
                .status(entity.getStatus().name())
                .checkInTime(entity.getCheckInTime())
                .checkOutTime(entity.getCheckOutTime())
                .build();
    }

    private LeaveResponse mapToLeaveResponse(LeaveRequestEntity entity) {
        return LeaveResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .employeeName(entity.getUser().getFullName())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .reason(entity.getReason())
                .status(entity.getStatus().name())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private SalaryResponse mapToSalaryResponse(SalaryEntity entity) {
        return SalaryResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .employeeName(entity.getUser().getFullName())
                .amount(entity.getAmount())
                .paymentDate(entity.getPaymentDate())
                .build();
    }

    private NotificationResponse mapToNotificationResponse(NotificationEntity entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .message(entity.getMessage())
                .type(entity.getType().name())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
