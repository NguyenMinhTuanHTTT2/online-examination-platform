package com.tuan.exam.service.interfaces;

import com.tuan.exam.dto.request.LoginRequest;
import com.tuan.exam.dto.request.RegisterRequest;
import com.tuan.exam.dto.response.JwtAuthResponse;

public interface AuthService {
    JwtAuthResponse login(LoginRequest loginRequest);
    String register(RegisterRequest registerRequest);
}
