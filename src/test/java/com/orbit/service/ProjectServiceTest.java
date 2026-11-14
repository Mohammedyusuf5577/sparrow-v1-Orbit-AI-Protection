package com.orbit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbit.dto.request.ProjectCreateRequest;
import com.orbit.dto.request.ProjectUpdateRequest;
import com.orbit.dto.response.PageResponse;
import com.orbit.dto.response.ProjectDetailResponse;
import com.orbit.entity.NotificationOutboxEntity;
import com.orbit.entity.ProjectAssignmentEntity;
import com.orbit.entity.ProjectEntity;
import com.orbit.entity.UserEntity;
import com.orbit.repository.NotificationOutboxRepository;
import com.orbit.repository.ProjectAssignmentRepository;
import com.orbit.repository.ProjectRepository;
import com.orbit.repository.UserRepository;
import com.orbit.service.impl.ProjectServiceImpl;
import com.orbit.util.AppUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationOutboxRepository notificationOutboxRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Direct caller-runs executor for deterministic tests
    private final Executor testExecutor = Runnable::run;

    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectServiceImpl(
                projectRepository,
                projectAssignmentRepository,
                userRepository,
                notificationOutboxRepository,
                objectMapper,
                testExecutor
        );
    }

    @Test
    void createProject_withAssignees_shouldSaveProjectAssignmentsAndEnqueuesOutbox() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .fullName("Jane Doe")
                .email("jane@orbit.com")
                .phone("+919876543210")
                .role(AppUtils.UserRole.EMPLOYEE)
                .status(AppUtils.UserStatus.ACTIVE)
                .build();

        ProjectCreateRequest request = ProjectCreateRequest.builder()
                .name("Orbit AI Security")
                .clientName("Google DeepMind")
                .description("Automated protection")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(3))
                .status(AppUtils.ProjectStatus.IN_PROGRESS)
                .assigneeIds(List.of(userId))
                .build();

        UUID projectId = UUID.randomUUID();
        ProjectEntity savedProject = ProjectEntity.builder()
                .id(projectId)
                .name(request.getName())
                .clientName(request.getClientName())
                .description(request.getDescription())
                .status(request.getStatus())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdAt(LocalDateTime.now())
                .build();

        when(projectRepository.save(any(ProjectEntity.class))).thenReturn(savedProject);
        when(userRepository.findAllById(any())).thenReturn(List.of(user));

        ProjectDetailResponse response = projectService.createProject(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(projectId);
        assertThat(response.getName()).isEqualTo("Orbit AI Security");
        assertThat(response.getAssignedUserCount()).isEqualTo(1);

        // Verify batch save calls
        verify(projectAssignmentRepository).saveAll(any());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<NotificationOutboxEntity>> outboxCaptor = ArgumentCaptor.forClass(List.class);
        verify(notificationOutboxRepository).saveAll(outboxCaptor.capture());

        List<NotificationOutboxEntity> outboxList = outboxCaptor.getValue();
        assertThat(outboxList).hasSize(1);
        NotificationOutboxEntity outbox = outboxList.get(0);
        assertThat(outbox.getStatus()).isEqualTo("PENDING");
        assertThat(outbox.getRecipientPhone()).isEqualTo("+919876543210");
        assertThat(outbox.getTemplateName()).isEqualTo("project_assignment");
    }

    @Test
    void updateProject_shouldDiffAssignmentsAndTriggerOutboxOnlyForNewAssignees() {
        UUID projectId = UUID.randomUUID();
        UUID existingUserId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();

        ProjectEntity existingProject = ProjectEntity.builder()
                .id(projectId)
                .name("Old Name")
                .clientName("Old Client")
                .status(AppUtils.ProjectStatus.IN_PROGRESS)
                .build();

        UserEntity existingUser = UserEntity.builder().id(existingUserId).fullName("User 1").role(AppUtils.UserRole.EMPLOYEE).build();
        UserEntity newUser = UserEntity.builder().id(newUserId).fullName("User 2").phone("+919876543211").role(AppUtils.UserRole.EMPLOYEE).build();

        ProjectAssignmentEntity existingAssignment = ProjectAssignmentEntity.builder()
                .project(existingProject)
                .instructor(existingUser)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(projectRepository.save(any(ProjectEntity.class))).thenReturn(existingProject);
        when(projectAssignmentRepository.findByProjectId(projectId)).thenReturn(List.of(existingAssignment));
        when(userRepository.findAllById(Set.of(newUserId))).thenReturn(List.of(newUser));
        when(notificationOutboxRepository.existsByUserIdAndProjectIdAndStatus(newUserId, projectId, "SENT")).thenReturn(false);

        // For getProjectById at the end of update
        when(projectAssignmentRepository.findByProjectIdWithUser(projectId)).thenReturn(List.of(
                existingAssignment,
                ProjectAssignmentEntity.builder().project(existingProject).instructor(newUser).build()
        ));

        ProjectUpdateRequest updateRequest = ProjectUpdateRequest.builder()
                .name("New Name")
                .clientName("New Client")
                .status(AppUtils.ProjectStatus.IN_PROGRESS)
                .assigneeIds(List.of(existingUserId, newUserId)) // Keeps existing, adds new
                .build();

        ProjectDetailResponse result = projectService.updateProject(projectId, updateRequest);

        assertThat(result).isNotNull();
        // Verify outbox was only saved for newUserId
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<NotificationOutboxEntity>> outboxCaptor = ArgumentCaptor.forClass(List.class);
        verify(notificationOutboxRepository).saveAll(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue()).hasSize(1);
        assertThat(outboxCaptor.getValue().get(0).getUser().getId()).isEqualTo(newUserId);
    }
}
