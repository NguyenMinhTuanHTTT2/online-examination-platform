package com.tuan.exam.service.impl;

import com.tuan.exam.dto.request.LoginRequest;
import com.tuan.exam.dto.request.RegisterRequest;
import com.tuan.exam.dto.response.JwtAuthResponse;
import com.tuan.exam.entity.Role;
import com.tuan.exam.entity.User;
import com.tuan.exam.entity.enums.UserStatus;
import com.tuan.exam.repository.RoleRepository;
import com.tuan.exam.repository.UserRepository;
import com.tuan.exam.security.JwtTokenProvider;
import com.tuan.exam.service.interfaces.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
        private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Override
    public JwtAuthResponse login(LoginRequest loginRequest) {
        // 1. Xác thực username và password thông qua Spring Security AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 2. Nạp thông tin xác thực vào Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Sinh JWT Token
        String token = tokenProvider.generateToken(authentication);

        // 4. Lấy thông tin chi tiết User để trả về DTO
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));

        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return JwtAuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public String register(RegisterRequest registerRequest) {
        try{
            // 1. Kiểm tra username đã tồn tại chưa
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                throw new RuntimeException("Tên đăng nhập đã được sử dụng!");
            }

            // 2. Kiểm tra email đã tồn tại chưa
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                throw new RuntimeException("Email đã được đăng ký!");
            }

            // 3. Khởi tạo đối tượng User mới
            User user = User.builder()
                    .username(registerRequest.getUsername())
                    .email(registerRequest.getEmail())
                    .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                    .fullName(registerRequest.getFullName())
                    .status(UserStatus.ACTIVE)
                    .roles(new HashSet<>())
                    .build();

            // 4. Gán vai trò mặc định ROLE_STUDENT cho người dùng mới
            Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                    .orElseThrow(() -> new RuntimeException("Lỗi: Vai trò ROLE_STUDENT không tồn tại trong DB!"));

            user.getRoles().add(studentRole);

            // 5. Lưu vào Database
            userRepository.save(user);

            return "Đăng ký tài khoản thành công!";
        } catch (Exception e) {
            System.err.println("=== LỖI ĐĂNG KÝ THỰC TẾ LÀ: ===");
            e.printStackTrace(); // In t
            throw e;
        }
    }
}
