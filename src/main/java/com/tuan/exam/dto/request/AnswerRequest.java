package com.tuan.exam.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerRequest {
    private Long id;

    @NotBlank(message = "Nội dung đáp án không được để trống")
    private String content;

    @NotNull(message = "Phải xác định đáp án đúng/sai")
    @JsonProperty("isCorrect") // ⚠️ BẮT BUỘC có annotation này!
    private Boolean isCorrect;
}