package com.tuan.exam.service.interfaces;

import com.tuan.exam.dto.response.QuizStatisticResponse;

public interface QuizStatisticService {
    QuizStatisticResponse getQuizStatistics(Long quizId, String username);
}
