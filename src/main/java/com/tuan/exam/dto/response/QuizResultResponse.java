package com.tuan.exam.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class QuizResultResponse {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;

    private Integer scoreObtained;  // Số điểm đạt được
    private Integer totalMarks;     // Tổng điểm tối đa của đề
    private Double percentage;      // Điểm phần trăm (%)
    private Boolean isPassed;       // Đạt hay Không đạt

    private Integer totalQuestions;
    private Integer correctCount;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long durationSeconds;   // Thời gian làm bài (giây)

    // Chi tiết kết quả từng câu (Chỉ trả về nếu QuizSettings.allowReview = true)
    private List<QuestionResultDto> details;
}
