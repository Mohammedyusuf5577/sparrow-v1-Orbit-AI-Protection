package com.orbit.config;

import com.orbit.entity.UserEntity;
import com.orbit.repository.UserRepository;
import com.orbit.util.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        // Ensure schema columns are present
        try {
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20)");
            jdbcTemplate.execute(
                    "ALTER TABLE projects ADD COLUMN IF NOT EXISTS client_name VARCHAR(255) NOT NULL DEFAULT ''");
            jdbcTemplate.execute("ALTER TABLE projects ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP");
            jdbcTemplate.execute("ALTER TABLE project_assignments ADD COLUMN IF NOT EXISTS assigned_by UUID");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_projects_deleted_at ON projects(deleted_at)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_projects_client_name ON projects(client_name)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone)");
            log.info("Schema migrations verified successfully.");
        } catch (Exception e) {
            log.warn("Schema migration verification notice: {}", e.getMessage());
        }

        // Ensure default admin exists
        createAdminIfMissing("admin@orbit.com", "ORBIT Admin");
        // Ensure user's email is also an admin if that's what they intend
        createAdminIfMissing("Yusuf786@gmail.com", "Yusuf Admin");
    }

    private void createAdminIfMissing(String email, String fullName) {
        if (!userRepository.existsByEmailIgnoreCase(email)) {
            UserEntity admin = UserEntity.builder()
                    .email(email.toLowerCase())
                    .fullName(fullName)
                    .password("Orbit@123")
                    .role(AppUtils.UserRole.ADMIN)
                    .status(AppUtils.UserStatus.ACTIVE)
                    .build();
            userRepository.save(admin);
            log.info("Created default admin user: {}", email);
        } else {
            // Ensure existing user with this email is an ADMIN to avoid login issues
            userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
                boolean updated = false;
                if (user.getRole() != AppUtils.UserRole.ADMIN) {
                    user.setRole(AppUtils.UserRole.ADMIN);
                    updated = true;
                }
                if (user.getStatus() != AppUtils.UserStatus.ACTIVE) {
                    user.setStatus(AppUtils.UserStatus.ACTIVE);
                    updated = true;
                }
                if (updated) {
                    userRepository.save(user);
                    log.info("Updated existing user to ADMIN and ACTIVE: {}", email);
                }
            });
        }
    }
}
