package com.tuan.exam.service.interfaces;

import java.util.List;
import java.util.Map;

public interface StudentDashboardService {
    Map<String, Object> getStudentStats(String username);
    List<Map<String, Object>> getScoreDistribution(String username);
    Map<String, Object> getFeaturedExam();
    List<Map<String, Object>> getPendingExams(String username);
    List<Map<String, Object>> getSuggestedExams(String username);
    List<Map<String, Object>> getLearningProgress(String username, String range);
}
