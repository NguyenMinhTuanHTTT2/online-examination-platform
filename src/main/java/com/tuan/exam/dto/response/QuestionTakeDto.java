package com.tuan.exam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionTakeDto {
    private Integer questionOrder;
    private Long questionId;
    private String content;
    private List<OptionDto> options;
}
