package com.tuan.exam.service.interfaces;

import com.tuan.exam.dto.request.QuizRequest;
import com.tuan.exam.dto.response.QuizResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuizService {
    QuizResponse createQuiz(QuizRequest request, String username);
    QuizResponse updateQuiz(Long id, QuizRequest request, String username);
    QuizResponse publishQuiz(Long id, String username);
    QuizResponse getQuizById(Long id);
    Page<QuizResponse> getMyQuizzes(String username, Pageable pageable);
}
