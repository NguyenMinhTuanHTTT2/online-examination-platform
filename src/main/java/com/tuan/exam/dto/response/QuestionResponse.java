package com.tuan.exam.dto.response;

import com.tuan.exam.entity.enums.QuestionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class QuestionResponse {
    private Long id;
    private String content;
    private QuestionType type;
    private Integer scoreWeight;
    private String explanation;
    private List<AnswerResponse> answers;
}
