package com.tuan.exam.service.interfaces;

import com.tuan.exam.dto.response.MockTestResponse;

import java.util.List;
import java.util.Map;

public interface MockTestService {
    List<MockTestResponse> getStudentMockTests(String search, Long subjectId, String status, String username);
    Map<String, Object> startMockTest(Long quizId, String username);
}
