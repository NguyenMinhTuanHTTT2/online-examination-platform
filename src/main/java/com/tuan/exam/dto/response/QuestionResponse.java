package com.tuan.exam.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tuan.exam.entity.enums.Difficulty;
import com.tuan.exam.entity.enums.QuestionType;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@Builder
public class QuestionResponse {
    private Long id;
    private String content;
    @JsonProperty("question_type")
    private QuestionType type;
    private Difficulty difficulty;
    private Integer scoreWeight;
    private Long subjectId;
    private String explanation;
    private List<AnswerResponse> answers;
}
