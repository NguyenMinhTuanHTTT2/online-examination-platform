package com.tuan.exam.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tuan.exam.entity.enums.Difficulty;
import com.tuan.exam.entity.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {
    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    @JsonProperty("content")
    private String content;

    @NotNull(message = "Loại câu hỏi không được để trống")
    @JsonProperty("question_type")
    private QuestionType questionType;

    @NotNull(message = "Điểm của câu hỏi không được để trống")
    @JsonProperty("scoreWeight")
    private Integer scoreWeight;

    @NotNull(message = "Mức độ khó không được để trống")
    @JsonProperty("difficulty")
    private Difficulty difficulty;

    @NotNull(message = "Môn học không được để trống")
    @JsonProperty("subjectId")
    private Long subjectId;

    @JsonProperty("explanation")
    private String explanation;

    @Valid
    @JsonProperty("answers")
    private List<AnswerRequest> answers;
}
