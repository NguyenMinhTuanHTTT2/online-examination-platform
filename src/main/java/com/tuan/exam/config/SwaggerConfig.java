package com.tuan.exam.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Exam Platform API Documentation",
                version = "1.0",
                description = "Tài liệu API cho hệ thống thi trắc nghiệm trực tuyến (Quiz Management System).",
                contact = @Contact(name = "Developer Name", email = "dev@gmail.com")
        ),
        // Áp dụng bảo mật JWT mặc định cho toàn bộ các API
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Nhập JWT Token nhận được sau khi gọi API /api/v1/auth/login"
)

public class SwaggerConfig {
}
