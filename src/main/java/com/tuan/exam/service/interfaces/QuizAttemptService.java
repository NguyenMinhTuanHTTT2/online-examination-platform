package com.tuan.exam.service.interfaces;

import com.tuan.exam.dto.request.SubmitQuizRequest;
import com.tuan.exam.dto.response.QuizResultResponse;
import com.tuan.exam.entity.QuizAttempt;

public interface QuizAttemptService {
    Long startQuizAttempt(Long quizId, String username);
    QuizResultResponse submitQuizAttempt(Long attemptId, SubmitQuizRequest request, String username);
    QuizResultResponse getAttemptResult(Long attemptId, String username);
    void autoSave(Long attemptId, SubmitQuizRequest request, String username);
    QuizResultResponse reportViolation(Long attemptId, String username);
    QuizResultResponse forceSubmitDraft(QuizAttempt attempt);
}
