package com.tuan.exam.dto.request;

import com.tuan.exam.dto.QuizSettingsDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;

    // Code vào thi, nếu để trống hệ thống sẽ tự sinh ngẫu nhiên
    private String code;

    @NotNull(message = "Cấu hình bài thi không được để trống")
    private QuizSettingsDto settings;
}

