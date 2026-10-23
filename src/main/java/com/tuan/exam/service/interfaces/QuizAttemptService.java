    package com.tuan.exam.service.interfaces;

    import com.tuan.exam.dto.request.SubmitQuizRequest;
    import com.tuan.exam.dto.response.QuizResultResponse;
    import com.tuan.exam.entity.QuizAttempt;

    import java.util.Map;

    /*trung tâm xử lý toàn bộ quá trình thi trực tiếp của học viên (từ lúc bắt đầu,
    làm bài, nộp bài, cho đến khi bị xử lý vi phạm).   */
    public interface QuizAttemptService {
        Long startQuizAttempt(Long quizId, String username);
        Map<String, Object> getAttemptForTake(Long attemptId, String username);
        QuizResultResponse submitQuizAttempt(Long attemptId, SubmitQuizRequest request, String username);
        QuizResultResponse getAttemptResult(Long attemptId, String username);
        void autoSave(Long attemptId, SubmitQuizRequest request, String username);
        QuizResultResponse reportViolation(Long attemptId, String username);
        QuizResultResponse forceSubmitDraft(QuizAttempt attempt);
    }
