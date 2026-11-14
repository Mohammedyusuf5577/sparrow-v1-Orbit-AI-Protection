package com.orbit.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbit.dto.request.ProjectCreateRequest;
import com.orbit.dto.request.ProjectUpdateRequest;
import com.orbit.dto.response.AssignedUserSummary;
import com.orbit.dto.response.PageResponse;
import com.orbit.dto.response.ProjectDetailResponse;
import com.orbit.entity.NotificationOutboxEntity;
import com.orbit.entity.ProjectAssignmentEntity;
import com.orbit.entity.ProjectEntity;
import com.orbit.entity.UserEntity;
import com.orbit.exception.AppException;
import com.orbit.repository.NotificationOutboxRepository;
import com.orbit.repository.ProjectAssignmentRepository;
import com.orbit.repository.ProjectRepository;
import com.orbit.repository.UserRepository;
import com.orbit.service.ProjectService;
import com.orbit.util.AppUtils;
import com.orbit.util.PageValidator;
import com.orbit.util.PhoneNormalizer;
import com.orbit.util.ProjectSortField;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final UserRepository userRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final ObjectMapper objectMapper;

    @Qualifier("dashboardExecutor")
    private final Executor dashboardExecutor;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectDetailResponse> getProjects(
            int page,
            int limit,
            String search,
            AppUtils.ProjectStatus status,
            Integer month,
            Integer year,
            String sortBy,
            String sortOrder
    ) {
        int validatedPage = PageValidator.validatePage(page);
        int validatedLimit = PageValidator.validateLimit(limit);
        String sortField = ProjectSortField.validateAndGet(sortBy);
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(validatedPage - 1, validatedLimit, Sort.by(direction, sortField));

        Specification<ProjectEntity> spec = buildSpecification(search, status, month, year);

        // Run count and data page in parallel using dashboardExecutor
        CompletableFuture<Long> countFuture = CompletableFuture.supplyAsync(
                () -> projectRepository.count(spec),
                dashboardExecutor
        );
        CompletableFuture<List<ProjectEntity>> itemsFuture = CompletableFuture.supplyAsync(
                () -> projectRepository.findAll(spec, pageable).getContent(),
                dashboardExecutor
        );

        CompletableFuture.allOf(countFuture, itemsFuture).join();

        long total = countFuture.join();
        List<ProjectEntity> projects = itemsFuture.join();

        if (projects.isEmpty()) {
            return PageResponse.of(Collections.emptyList(), validatedPage, validatedLimit, total);
        }

        // Batch load assignment counts for all retrieved projects in ONE query (No N+1)
        List<UUID> projectIds = projects.stream().map(ProjectEntity::getId).toList();
        Map<UUID, Integer> assignmentCounts = new HashMap<>();
        projectRepository.countAssignmentsByProjectIds(projectIds).forEach(row -> {
            UUID pId = (UUID) row[0];
            int count = ((Number) row[1]).intValue();
            assignmentCounts.put(pId, count);
        });

        LocalDateTime now = LocalDateTime.now();
        List<ProjectDetailResponse> dtos = projects.stream().map(p -> {
            AppUtils.ProjectStatus effectiveStatus = p.getStatus();
            if (effectiveStatus != AppUtils.ProjectStatus.COMPLETED && p.getEndDate() != null && p.getEndDate().isBefore(now)) {
                effectiveStatus = AppUtils.ProjectStatus.DELAYED;
            }
            return ProjectDetailResponse.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .clientName(p.getClientName() != null ? p.getClientName() : "")
                    .description(p.getDescription())
                    .startDate(p.getStartDate())
                    .endDate(p.getEndDate())
                    .status(effectiveStatus)
                    .assignedUserCount(assignmentCounts.getOrDefault(p.getId(), 0))
                    .deletedAt(p.getDeletedAt())
                    .createdAt(p.getCreatedAt())
                    .updatedAt(p.getUpdatedAt())
                    .build();
        }).toList();

        return PageResponse.of(dtos, validatedPage, validatedLimit, total);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectById(UUID id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new AppException(AppUtils.Status.PROJECT_NOT_FOUND));

        List<ProjectAssignmentEntity> assignments = projectAssignmentRepository.findByProjectIdWithUser(id);
        List<AssignedUserSummary> assignees = assignments.stream().map(pa -> {
            UserEntity user = pa.getInstructor();
            return AssignedUserSummary.builder()
                    .id(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .role(user.getRole() != null ? user.getRole().name() : null)
                    .build();
        }).toList();

        AppUtils.ProjectStatus effectiveStatus = project.getStatus();
        if (effectiveStatus != AppUtils.ProjectStatus.COMPLETED && project.getEndDate() != null && project.getEndDate().isBefore(LocalDateTime.now())) {
            effectiveStatus = AppUtils.ProjectStatus.DELAYED;
        }

        return ProjectDetailResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .clientName(project.getClientName() != null ? project.getClientName() : "")
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(effectiveStatus)
                .assignedUserCount(assignees.size())
                .assignees(assignees)
                .deletedAt(project.getDeletedAt())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public ProjectDetailResponse createProject(ProjectCreateRequest request) {
        ProjectEntity project = ProjectEntity.builder()
                .name(request.getName().trim())
                .clientName(request.getClientName().trim())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus() != null ? request.getStatus() : AppUtils.ProjectStatus.IN_PROGRESS)
                .build();

        ProjectEntity savedProject = projectRepository.save(project);
        List<AssignedUserSummary> assignees = new ArrayList<>();

        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            List<UUID> distinctUserIds = request.getAssigneeIds().stream().distinct().toList();
            List<UserEntity> users = userRepository.findAllById(distinctUserIds);
            UUID currentAdminId = AppUtils.getPrincipalUserId();

            List<ProjectAssignmentEntity> assignments = new ArrayList<>();
            List<NotificationOutboxEntity> outboxEntries = new ArrayList<>();

            for (UserEntity user : users) {
                assignments.add(ProjectAssignmentEntity.builder()
                        .project(savedProject)
                        .instructor(user)
                        .assignedBy(currentAdminId)
                        .assignedAt(LocalDateTime.now())
                        .build());

                outboxEntries.add(buildOutboxRecord(user, savedProject));

                assignees.add(AssignedUserSummary.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(user.getRole() != null ? user.getRole().name() : null)
                        .build());
            }

            projectAssignmentRepository.saveAll(assignments);
            if (!outboxEntries.isEmpty()) {
                notificationOutboxRepository.saveAll(outboxEntries);
            }
        }

        return ProjectDetailResponse.builder()
                .id(savedProject.getId())
                .name(savedProject.getName())
                .clientName(savedProject.getClientName())
                .description(savedProject.getDescription())
                .startDate(savedProject.getStartDate())
                .endDate(savedProject.getEndDate())
                .status(savedProject.getStatus())
                .assignedUserCount(assignees.size())
                .assignees(assignees)
                .createdAt(savedProject.getCreatedAt())
                .updatedAt(savedProject.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public ProjectDetailResponse updateProject(UUID id, ProjectUpdateRequest request) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new AppException(AppUtils.Status.PROJECT_NOT_FOUND));

        project.setName(request.getName().trim());
        project.setClientName(request.getClientName().trim());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        ProjectEntity updatedProject = projectRepository.save(project);

        // Perform assignment diffing in Java (O(N) Set operations)
        List<ProjectAssignmentEntity> existingAssignments = projectAssignmentRepository.findByProjectId(id);
        Set<UUID> existingUserIds = existingAssignments.stream()
                .map(pa -> pa.getInstructor().getId())
                .collect(Collectors.toSet());

        Set<UUID> requestedUserIds = request.getAssigneeIds() != null
                ? new HashSet<>(request.getAssigneeIds())
                : Collections.emptySet();

        Set<UUID> toRemove = new HashSet<>(existingUserIds);
        toRemove.removeAll(requestedUserIds);

        Set<UUID> toAdd = new HashSet<>(requestedUserIds);
        toAdd.removeAll(existingUserIds);

        if (!toRemove.isEmpty()) {
            projectAssignmentRepository.deleteByProjectIdAndInstructorIds(id, toRemove);
        }

        if (!toAdd.isEmpty()) {
            List<UserEntity> newUsers = userRepository.findAllById(toAdd);
            UUID currentAdminId = AppUtils.getPrincipalUserId();
            List<ProjectAssignmentEntity> newAssignments = new ArrayList<>();
            List<NotificationOutboxEntity> outboxEntries = new ArrayList<>();

            for (UserEntity user : newUsers) {
                newAssignments.add(ProjectAssignmentEntity.builder()
                        .project(updatedProject)
                        .instructor(user)
                        .assignedBy(currentAdminId)
                        .assignedAt(LocalDateTime.now())
                        .build());

                // Check idempotency before inserting
                boolean alreadySent = notificationOutboxRepository.existsByUserIdAndProjectIdAndStatus(
                        user.getId(), id, "SENT");
                if (!alreadySent) {
                    outboxEntries.add(buildOutboxRecord(user, updatedProject));
                }
            }

            projectAssignmentRepository.saveAll(newAssignments);
            if (!outboxEntries.isEmpty()) {
                notificationOutboxRepository.saveAll(outboxEntries);
            }
        }

        // Return updated project detail
        return getProjectById(id);
    }

    @Override
    @Transactional
    public void softDeleteProject(UUID id) {
        int updated = projectRepository.softDeleteById(id, LocalDateTime.now());
        if (updated == 0) {
            throw new AppException(AppUtils.Status.PROJECT_NOT_FOUND);
        }
    }

    private NotificationOutboxEntity buildOutboxRecord(UserEntity user, ProjectEntity project) {
        String normalizedPhone = PhoneNormalizer.normalize(user.getPhone());
        boolean hasValidPhone = normalizedPhone != null;

        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("employeeName", user.getFullName() != null ? user.getFullName() : "Team Member");
        payloadMap.put("projectName", project.getName());
        payloadMap.put("clientName", project.getClientName() != null ? project.getClientName() : "");
        payloadMap.put("startDate", project.getStartDate() != null ? project.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
        payloadMap.put("endDate", project.getEndDate() != null ? project.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadMap);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize notification outbox payload for user {}", user.getId(), e);
            jsonPayload = "{}";
        }

        if (!hasValidPhone) {
            return NotificationOutboxEntity.builder()
                    .user(user)
                    .project(project)
                    .channel("WHATSAPP")
                    .recipientPhone(user.getPhone() != null ? user.getPhone() : "")
                    .templateName("project_assignment")
                    .payload(jsonPayload)
                    .status("FAILED")
                    .errorLog("User phone number is missing or not in valid E.164 format")
                    .build();
        }

        return NotificationOutboxEntity.builder()
                .user(user)
                .project(project)
                .channel("WHATSAPP")
                .recipientPhone(normalizedPhone)
                .templateName("project_assignment")
                .payload(jsonPayload)
                .status("PENDING")
                .attempts(0)
                .maxAttempts(3)
                .build();
    }

    private Specification<ProjectEntity> buildSpecification(
            String search,
            AppUtils.ProjectStatus status,
            Integer month,
            Integer year
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search filter: lower(name) or lower(clientName)
            if (search != null && !search.trim().isEmpty()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), term),
                        cb.like(cb.lower(root.get("clientName")), term)
                ));
            }

            // Status filter: delayed computed dynamically
            if (status != null) {
                if (status == AppUtils.ProjectStatus.DELAYED) {
                    predicates.add(cb.or(
                            cb.equal(root.get("status"), AppUtils.ProjectStatus.DELAYED),
                            cb.and(
                                    cb.lessThan(root.get("endDate"), LocalDateTime.now()),
                                    cb.notEqual(root.get("status"), AppUtils.ProjectStatus.COMPLETED)
                            )
                    ));
                } else {
                    predicates.add(cb.equal(root.get("status"), status));
                }
            }

            // Date filtering via index-friendly range bounds
            if (year != null && month != null) {
                YearMonth ym = YearMonth.of(year, month);
                LocalDateTime start = ym.atDay(1).atStartOfDay();
                LocalDateTime end = ym.atEndOfMonth().atTime(LocalTime.MAX);
                predicates.add(cb.between(root.get("startDate"), start, end));
            } else if (year != null) {
                LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
                LocalDateTime end = LocalDate.of(year, 12, 31).atTime(LocalTime.MAX);
                predicates.add(cb.between(root.get("startDate"), start, end));
            } else if (month != null) {
                int currentYear = LocalDate.now().getYear();
                YearMonth ym = YearMonth.of(currentYear, month);
                LocalDateTime start = ym.atDay(1).atStartOfDay();
                LocalDateTime end = ym.atEndOfMonth().atTime(LocalTime.MAX);
                predicates.add(cb.between(root.get("startDate"), start, end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
