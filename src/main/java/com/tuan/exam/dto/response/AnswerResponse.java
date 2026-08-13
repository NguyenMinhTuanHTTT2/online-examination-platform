package com.tuan.exam.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class AnswerResponse {
    private Long id;
    private String content;
    private Boolean isCorrect;
}
