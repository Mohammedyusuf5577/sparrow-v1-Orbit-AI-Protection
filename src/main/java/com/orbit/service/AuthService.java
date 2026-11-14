package com.orbit.service;

import com.orbit.dto.request.LoginRequest;
import com.orbit.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);
}

