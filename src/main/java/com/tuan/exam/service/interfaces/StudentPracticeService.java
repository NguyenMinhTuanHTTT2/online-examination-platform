package com.tuan.exam.service.interfaces;

import java.util.List;
import java.util.Map;

public interface StudentPracticeService {
    Map<String, Object> getFilterCategories();
    List<Map<String, Object>> getPracticeQuizzes(Long subjectId, Long topicId, String difficulty, String sortBy, String search);
    Map<String, Object> startPractice(Long quizId, String username);
}
