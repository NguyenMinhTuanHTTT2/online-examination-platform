package com.tuan.exam.service.interfaces;

import com.tuan.exam.dto.request.QuizRequest;
import com.tuan.exam.dto.response.QuizResponse;
import com.tuan.exam.dto.response.QuizTakeResponse;
import com.tuan.exam.entity.QuizAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuizService {
    QuizResponse createQuiz(QuizRequest request, String username);
    QuizResponse updateQuiz(Long id, QuizRequest request, String username);
    QuizResponse publishQuiz(Long id, String username);
    QuizResponse getQuizById(Long id);
    Page<QuizResponse> getMyQuizzes(String username, Pageable pageable);
    List<QuizResponse> getPublishedQuizzes();
    void deleteQuiz(Long id, String username);

    // Bổ sung phương thức cho lượt làm bài
    QuizAttempt startQuizAttempt(Long quizId, String username);
    QuizTakeResponse getQuizTakeData(Long attemptId, String username);
}
