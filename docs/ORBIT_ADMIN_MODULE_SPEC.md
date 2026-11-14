# ORBIT - Admin Module Specification
**Project**: AI Based Organizational Risk & Intelligence Platform  
**Module**: ADMIN MODULE ONLY  
**Version**: 1.0.0  
**Status**: Specification / Blueprint

---

## 1. Technology Stack

### Backend
*   **Language**: Java 17
*   **Framework**: Spring Boot 3.x
*   **Build Tool**: Gradle (Groovy DSL)
*   **Database**: PostgreSQL
*   **Migration**: Flyway
*   **Security**: Spring Security + JWT
*   **Documentation**: Swagger (OpenAPI 3)
*   **Notifications**: Firebase Cloud Messaging (FCM)
*   **Scheduling**: Spring Scheduler (for AI monitoring)
*   **Utility**: Lombok, MapStruct (for DTO mapping)

### Frontend
*   **Library**: React 18
*   **Language**: TypeScript
*   **Styling**: Tailwind CSS
*   **Routing**: React Router DOM 6
*   **State/API**: Context API + Axios (with interceptors)
*   **UI Style**: Minimal, "Razorpay-inspired" clean design (White/Gray/Professional)

---

## 2. Architecture & Project Structure

### Backend (`com.orbit`)
Following the **PrepBuddy-style** layered architecture. **Strict Rules**:
*   No Entity exposure in Controllers.
*   Use DTOs for all data transfer.
*   `Response<T>` wrapper for all API responses.
*   Global Exception Handling.

```text
src/main/java/com/orbit/
├── config/              # Security, Swagger, CORS, Firebase, Scheduler configs
├── controller/          # REST Controllers (Admin only)
├── dto/                 # Data Transfer Objects (Request/Response)
├── entity/              # JPA Entities
├── exception/           # GlobalExceptionHandler & Custom Exceptions
├── firebase/            # FCM Service & Logic
├── mapper/              # Entity <-> DTO Mappers
├── repository/          # Spring Data JPA Repositories
├── security/            # JWT Filter, UserDetailsService, Auth Entry Point
├── service/             # Business Logic Interfaces
├── service/impl/        # Business Logic Implementations
├── scheduler/           # AI Monitoring Scheduled Jobs
└── util/                # Constants, Helper classes
```

### Frontend (`src/`)
```text
src/
├── api/                 # Axios instance, Service calls
├── assets/              # Images, Static files
├── components/          # Reusable UI components (Cards, Tables, Graphs)
├── hooks/               # Custom hooks (useAuth, useFetch)
├── layout/              # Sidebar, Navbar, AdminLayout
├── pages/               # Admin Landing, Dashboard, Employees, Projects, Leaves, AI
├── routes/              # ProtectedRoute, AppRoutes
├── store/               # Context/Redux store (AuthContext)
├── types/               # TypeScript interfaces
└── utils/               # Formatters, Constants
```

---

## 3. Database Schema (PostgreSQL)

**Concept**: All primary keys are `UUID`.  
**Migration**: `src/main/resources/db/migration/V1__initial_schema.sql`

### Tables

1.  **users**
    *   `id` (UUID, PK)
    *   `email` (VARCHAR, Unique)
    *   `password` (VARCHAR) - *Admin only needs email, but struct supports others*
    *   `full_name` (VARCHAR)
    *   `role` (ENUM: BENCH, ADMIN, INSTRUCTOR, EMPLOYEE)
    *   `status` (ENUM: ACTIVE, INACTIVE)
    *   `created_at`, `updated_at`

2.  **projects**
    *   `id` (UUID, PK)
    *   `name` (VARCHAR)
    *   `description` (TEXT)
    *   `start_date`, `end_date` (TIMESTAMP)
    *   `status` (ENUM: COMPLETED, IN_PROGRESS, DELAYED)
    *   `created_at`, `updated_at`

3.  **project_assignments**
    *   `id` (UUID, PK)
    *   `project_id` (FK -> projects)
    *   `instructor_id` (FK -> users)
    *   `assigned_at`

4.  **tasks**
    *   `id` (UUID, PK)
    *   `project_id` (FK -> projects)
    *   `assigned_to` (FK -> users [Employee])
    *   `title`, `status` (PENDING, DONE)
    *   `due_date`

5.  **attendance**
    *   `id` (UUID, PK)
    *   `user_id` (FK -> users)
    *   `date` (DATE)
    *   `status` (PRESENT, ABSENT, LATE)
    *   `check_in_time`, `check_out_time`

6.  **leave_requests**
    *   `id` (UUID, PK)
    *   `user_id` (FK -> users)
    *   `start_date`, `end_date`
    *   `reason`
    *   `status` (PENDING, APPROVED, REJECTED)

7.  **salaries**
    *   `id` (UUID, PK)
    *   `user_id` (FK -> users)
    *   `amount` (DECIMAL)
    *   `payment_date`

8.  **notifications** (AI Generated)
    *   `id` (UUID, PK)
    *   `user_id` (FK -> users [Admin])
    *   `message` (TEXT)
    *   `type` (RISK_ALERT, INFO)
    *   `is_read` (BOOLEAN)
    *   `created_at`

---

## 4. Authentication Flow

**Role-Based Access Control (RBAC)**
*   **Roles**: `ADMIN`, `INSTRUCTOR`, `EMPLOYEE`
*   **Login**:
    *   `POST /api/auth/login`
    *   **Admin**: Email only (Mock password or specific flow if specified, prompts says "Only email required for login" implies a simplified flow or magic link, but standard JWT usually requires a secret. We will implement "Email Only" Login for Admin as requested - likely checking if email exists and is Admin).
*   **Response**:
    *   JWT Token (with Role Claim)
    *   User Details
*   **Security Filter**:
    *   Intercepts every request.
    *   Validates Bearer Token.
    *   Checks Role permissions.

---

## 5. Admin Functionalities & API Specification

### A. Employee Monitoring
*   `GET /api/admin/employees` - List all employees
*   `GET /api/admin/instructors` - List all instructors
*   `GET /api/admin/employees/{id}` - Details
*   `GET /api/admin/salaries` - View salary data

### B. Project Management
*   `POST /api/admin/projects` - Create
*   `GET /api/admin/projects` - List (with status filters)
*   `PUT /api/admin/projects/{id}` - Update
*   `DELETE /api/admin/projects/{id}` - Delete
*   `POST /api/admin/projects/assign` - Assign Instructor to Project
*   **Logic**: Check for "Delayed" status based on `end_date < now()` and `status != COMPLETED`.

### C. Attendance & Leave
*   `GET /api/admin/attendance` - View logs
*   `GET /api/admin/leaves` - View pending/all requests
*   `PUT /api/admin/leaves/{id}/approve`
*   `PUT /api/admin/leaves/{id}/reject`

### D. AI Monitoring System (Internal Logic)
*   **Component**: `AiMonitoringScheduler`
*   **Trigger**: `@Scheduled(cron = "...")`
*   **Logic**:
    1.  Fetch all Projects. Check `if (now > endDate && status != COMPLETED) -> Mark DELAYED`.
    2.  Check Instruction Inactivity (No login/updates in X days).
    3.  Count Pending Leaves.
    4.  Calculate **Risk Score**:
        *   Score = (DelayedProjects * 10) + (PendingLeaves * 5) + (AttendanceAnomalies * 5).
        *   Normalize to 0-100.
    5.  **Alert**:
        *   If Risk > Threshold (e.g., 50), save `Notification` entity.
        *   Trigger `FirebaseMessagingService`.
*   **API**:
    *   `GET /api/admin/ai-summary` -> Returns `{ activeEmployees, delayedProjects, riskScore, riskLevel }`

---

## 6. Frontend UI Flows

### Landing Page
*   **Components**: Centered Card, Role Dropdown, Email Input, "Login" Button.
*   **Action**: Calls Login API, stores JWT in `localStorage`, redirects into Admin Layout.

### Admin Dashboard (Layout)
*   **Sidebar**: Dashboard, Projects, Employees, Attendance, Leaves, AI Monitor, Logout.
*   **Navbar**: Notification Bell (shows AI alerts), Profile.

### Dashboard Examples
*   **Main Page**:
    *   **Cards**: Total Emp, Total Projects, Risk Score.
    *   **Graph**: Implementation of Project Hierarchy.
*   **AI Monitor Page**:
    *   Detailed breakdown of why the Risk Score is high.
    *   List of "High Risk" projects or employees.

---

## 7. Configuration Files (Preview)

### `build.gradle`
```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.postgresql:postgresql'
    implementation 'org.flywaydb:flyway-core'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'
    implementation 'com.google.firebase:firebase-admin:9.2.0'
}
```

### `application.properties`
```properties
spring.application.name=orbit-backend
spring.datasource.url=jdbc:postgresql://localhost:5432/orbit_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true

# JWT
jwt.secret=YOUR_SUPER_SECRET_KEY_MUST_BE_LONG_ENOUGH_FOR_HS256
jwt.expiration=86400000

# Scheduler
app.ai.monitoring.rate=3600000
```
