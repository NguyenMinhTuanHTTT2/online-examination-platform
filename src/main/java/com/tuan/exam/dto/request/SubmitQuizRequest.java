package com.tuan.exam.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubmitQuizRequest {
    @NotEmpty(message = "Danh sách câu trả lời không được để trống")
    @Valid
    private List<QuestionSubmissionDto> answers;
}
