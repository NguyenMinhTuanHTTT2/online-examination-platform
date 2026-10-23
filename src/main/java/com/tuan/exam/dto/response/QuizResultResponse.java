package com.tuan.exam.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@Builder
public class QuizResultResponse {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;

    private Double scoreObtained;  // Số điểm đạt được
    private Integer totalMarks;     // Tổng điểm tối đa của đề
    private Double percentage;      // Điểm phần trăm (%)
    private Boolean isPassed;       // Đạt hay Không đạt

    private Integer totalQuestions; // Tổng số câu
    private Integer correctCount;   // Số câu đúng
    private Integer incorrectCount; // Số câu sai (MỚI BỔ SUNG)
    private Integer unansweredCount;// Số câu bỏ trống (MỚI BỔ SUNG);

    private Instant startedAt;
    private Instant completedAt;
    private Long durationSeconds;   // Thời gian làm bài (giây)

    // Chi tiết kết quả từng câu (Chỉ trả về nếu QuizSettings.allowReview = true)
    private List<QuestionResultDto> details;
}
