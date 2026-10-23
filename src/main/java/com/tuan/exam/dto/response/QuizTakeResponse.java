package com.tuan.exam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizTakeResponse {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private Integer durationMinutes;
    private Instant startTime;
    private Instant endTime;
    private Long remainingSeconds;
    private Object draftAnswers;
    private List<QuestionTakeDto> questions;
}
