package com.tuan.exam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockTestResponse {
    private Long id;                  // Quiz ID
    private String title;             // Tên đề thi
    private String description;       // Mô tả
    private Long subjectId;           // ID môn học
    private String subjectName;       // Tên môn học (Ví dụ: Toán học, Tin học...)
    private Integer durationMinutes;  // Thời gian làm bài (phút)
    private Integer totalQuestions;   // Tổng số câu hỏi

    // Trạng thái đối với học viên: 'NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'
    private String status;

    private Long lastAttemptId;       // ID lượt làm bài (dùng cho nút "Tiếp tục thi" hoặc "Xem kết quả")
    private Double lastScore;         // Điểm của lượt làm bài gần nhất
    private Instant createdAt;        // Ngày tạo
}
