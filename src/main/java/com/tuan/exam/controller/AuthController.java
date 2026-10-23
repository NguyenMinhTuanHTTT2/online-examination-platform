package com.tuan.exam.controller;

import com.tuan.exam.dto.request.LoginRequest;
import com.tuan.exam.dto.request.RegisterRequest;
import com.tuan.exam.dto.response.JwtAuthResponse;
//import com.tuan.exam.service.interfaces.AuthService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
import com.tuan.exam.service.interfaces.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtAuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
        String response = authService.register(registerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 🔴 ENDPOINT LẤY THÔNG TIN USER ĐANG ĐĂNG NHẬP ĐỘNG TỪ CSDL
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập hoặc Token không hợp lệ");
        }

        // authentication.getName() sẽ trả về username hoặc email được mã hóa trong JWT
        String usernameOrEmail = authentication.getName();

        // Gọi AuthService để truy vấn thông tin User từ Database
        Object userProfile = authService.getCurrentUserProfile(usernameOrEmail);

        return ResponseEntity.ok(userProfile);
    }

}
