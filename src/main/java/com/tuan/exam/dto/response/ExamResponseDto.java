package com.tuan.exam.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ExamResponseDto {
    private Long id;
    private String title;
    private Long quizId;
    private String quizTitle;      // Tên bộ đề gốc
    private String subjectName;    // Tên môn học
    private String examType;       // "Đề luyện tập" hoặc "Đề thi thử"
    private Integer durationMinutes;
    private String status;         // Trạng thái kỳ thi
    private Long attemptsCount;    // Lượt tham gia làm bài
    private Double averageScore;   // Điểm trung bình
    private Instant createdAt;
}
