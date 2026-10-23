package com.tuan.exam.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@Data
public class AnswerResponse {
    private Long id;
    private String content;
    private Boolean isCorrect;
}
