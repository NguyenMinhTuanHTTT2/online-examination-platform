package com.tuan.exam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerRequest {
    private Long id; // Có thể null nếu là tạo mới

    @NotBlank(message = "Nội dung đáp án không được để trống")
    private String content;

    @NotNull(message = "Phải xác định đáp án đúng/sai")
    private Boolean isCorrect;
}
