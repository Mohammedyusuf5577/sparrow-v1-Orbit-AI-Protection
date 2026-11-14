-- ============================================
-- ORBIT Database Schema V1
-- AI Based Organizational Risk & Intelligence Platform
-- ============================================

-- 1. Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'EMPLOYEE',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Projects Table
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Project Assignments Table
CREATE TABLE project_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    instructor_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(project_id, instructor_id)
);

-- 4. Tasks Table
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    assigned_to UUID REFERENCES users(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    due_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. Attendance Table
CREATE TABLE attendance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PRESENT',
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    UNIQUE(user_id, date)
);

-- 6. Leave Requests Table
CREATE TABLE leave_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    reviewed_by UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. Salaries Table
CREATE TABLE salaries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount DECIMAL(12, 2) NOT NULL,
    payment_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 8. Notifications Table (AI Generated)
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'INFO',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indices for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_tasks_project ON tasks(project_id);
CREATE INDEX idx_tasks_assigned ON tasks(assigned_to);
CREATE INDEX idx_attendance_user ON attendance(user_id);
CREATE INDEX idx_attendance_date ON attendance(date);
CREATE INDEX idx_leave_requests_user ON leave_requests(user_id);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);

-- Seed data: Default Admin User
INSERT INTO users (id, email, password, full_name, role, status)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'admin@orbit.com',
    'admin',
    'ORBIT Admin',
    'ADMIN',
    'ACTIVE'
);

-- Seed: Sample Instructors
INSERT INTO users (email, password, full_name, role, status) VALUES
('instructor1@orbit.com', 'password', 'Priya Sharma', 'INSTRUCTOR', 'ACTIVE'),
('instructor2@orbit.com', 'password', 'Rahul Verma', 'INSTRUCTOR', 'ACTIVE');

-- Seed: Sample Employees
INSERT INTO users (email, password, full_name, role, status) VALUES
('employee1@orbit.com', 'password', 'Aisha Khan', 'EMPLOYEE', 'ACTIVE'),
('employee2@orbit.com', 'password', 'Vikram Singh', 'EMPLOYEE', 'ACTIVE'),
('employee3@orbit.com', 'password', 'Sneha Patel', 'EMPLOYEE', 'ACTIVE'),
('employee4@orbit.com', 'password', 'Arjun Reddy', 'EMPLOYEE', 'ACTIVE'),
('employee5@orbit.com', 'password', 'Meera Nair', 'EMPLOYEE', 'INACTIVE');

-- Seed: Sample Projects
INSERT INTO projects (name, description, start_date, end_date, status) VALUES
('Project Alpha', 'Core platform development', '2026-01-01', '2026-06-30', 'IN_PROGRESS'),
('Project Beta', 'Mobile app redesign', '2025-10-01', '2026-02-01', 'DELAYED'),
('Project Gamma', 'Data pipeline migration', '2025-08-01', '2025-12-31', 'COMPLETED');

-- Seed: Sample Attendance
INSERT INTO attendance (user_id, date, status, check_in_time, check_out_time)
SELECT u.id, CURRENT_DATE, 'PRESENT', CURRENT_DATE + TIME '09:00:00', CURRENT_DATE + TIME '18:00:00'
FROM users u WHERE u.email = 'employee1@orbit.com';

INSERT INTO attendance (user_id, date, status, check_in_time, check_out_time)
SELECT u.id, CURRENT_DATE, 'LATE', CURRENT_DATE + TIME '10:30:00', NULL
FROM users u WHERE u.email = 'employee2@orbit.com';

INSERT INTO attendance (user_id, date, status)
SELECT u.id, CURRENT_DATE, 'ABSENT'
FROM users u WHERE u.email = 'employee3@orbit.com';

-- Seed: Sample Leave Requests
INSERT INTO leave_requests (user_id, start_date, end_date, reason, status)
SELECT u.id, CURRENT_DATE + 3, CURRENT_DATE + 5, 'Family function', 'PENDING'
FROM users u WHERE u.email = 'employee1@orbit.com';

INSERT INTO leave_requests (user_id, start_date, end_date, reason, status)
SELECT u.id, CURRENT_DATE + 7, CURRENT_DATE + 10, 'Medical leave', 'PENDING'
FROM users u WHERE u.email = 'employee4@orbit.com';

-- Seed: Sample Salaries
INSERT INTO salaries (user_id, amount, payment_date)
SELECT u.id, 75000.00, '2026-01-31'
FROM users u WHERE u.email = 'employee1@orbit.com';

INSERT INTO salaries (user_id, amount, payment_date)
SELECT u.id, 65000.00, '2026-01-31'
FROM users u WHERE u.email = 'employee2@orbit.com';
