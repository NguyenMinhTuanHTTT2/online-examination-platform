package com.tuan.exam.service.interfaces;

import com.tuan.exam.dto.request.QuestionRequest;
import com.tuan.exam.dto.response.QuestionResponse;

import java.util.List;

public interface QuestionService {
    QuestionResponse addQuestionToQuiz(Long quizId, QuestionRequest request, String username);
    QuestionResponse updateQuestion(Long quizId, Long questionId, QuestionRequest request, String username);
    void deleteQuestion(Long quizId, Long questionId, String username);
    List<QuestionResponse> getQuestionsByQuizId(Long quizId, String username);
}
