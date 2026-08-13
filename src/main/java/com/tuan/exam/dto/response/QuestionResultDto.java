package com.tuan.exam.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class QuestionResultDto {
    private Long questionId;
    private String questionContent;
    private Boolean isCorrect;
    private Integer scoreEarned;
    private Integer maxScore;
    private List<Long> studentAnswerIds;
    private List<Long> correctAnswerIds;
    private String explanation;
}
