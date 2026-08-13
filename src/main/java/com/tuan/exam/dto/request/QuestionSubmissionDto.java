package com.tuan.exam.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

public class QuestionSubmissionDto {
    @NotNull(message = "Question ID không được để trống")
    private Long questionId;

    // Danh sách ID các đáp án học viên chọn (hỗ trợ cả Single Choice và Multiple Choice)
    private List<Long> selectedAnswerIds;

    // Đáp án dạng văn bản (cho câu hỏi Tự luận / Fill in the blank)
    private String textAnswer;
}
