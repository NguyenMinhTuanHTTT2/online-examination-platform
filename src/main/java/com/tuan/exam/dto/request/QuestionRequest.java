package com.tuan.exam.dto.request;

import com.tuan.exam.entity.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionRequest {
    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String content;

    @NotNull(message = "Loại câu hỏi không được để trống")
    private QuestionType type;

    @NotNull(message = "Điểm của câu hỏi không được để trống")
    private Integer scoreWeight;

    private String explanation;

    // Các đáp án đi kèm (Tự luận có thể không có đáp án)
    @Valid
    private List<AnswerRequest> answers;
}
