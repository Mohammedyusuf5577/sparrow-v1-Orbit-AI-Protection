package com.orbit.service.impl;

import com.orbit.exception.AppException;

import com.orbit.dto.request.LoginRequest;
import com.orbit.dto.response.AuthResponse;
import com.orbit.entity.UserEntity;

import com.orbit.repository.UserRepository;
import com.orbit.security.JwtService;
import com.orbit.service.AuthService;
import com.orbit.util.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        // Normalize email to trim whitespace; use case-insensitive lookup
        String email = loginRequest.getEmail().trim();
        log.info("Attempting login for email: {}", email);

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new AppException(AppUtils.Status.INVALID_CREDENTIALS);
                });

        log.info("User found: {}, Role: {}, Status: {}", user.getEmail(), user.getRole(), user.getStatus());

        // Admin email-only login as per spec
        if (user.getRole() != AppUtils.UserRole.ADMIN) {
            log.error("Login failed: User {} is not an ADMIN. Role: {}", user.getEmail(), user.getRole());
            throw new AppException(AppUtils.Status.ONLY_ADMIN_LOGIN);
        }

        if (user.getStatus() != AppUtils.UserStatus.ACTIVE) {
            log.error("Login failed: User {} is INACTIVE.", user.getEmail());
            throw new AppException(AppUtils.Status.ACCOUNT_INACTIVE);
        }

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getFullName());

        log.info(AppUtils.Status.ADMIN_LOGIN_SUCCESS_LOG.getMessage(user.getEmail()));

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }
}
