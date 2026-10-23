package com.tuan.exam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExamRequestDto {
    @NotBlank(message = "Tiêu đề kỳ thi không được để trống")
    private String title;

    @NotNull(message = "Phải chọn bộ đề gốc (Quiz ID)")
    private Long quizId;

    private String examType; // "Đề luyện tập" hoặc "Đề thi thử"

    @NotNull(message = "Thời gian làm bài không được để trống")
    private Integer durationMinutes; // Thời gian tính bằng phút

    private String status; // "DRAFT", "OPEN", "UPCOMING", "CLOSED"
}
