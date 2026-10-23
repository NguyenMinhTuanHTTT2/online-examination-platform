package com.tuan.exam.service.interfaces;

import com.tuan.exam.dto.request.ExamRequestDto;
import com.tuan.exam.dto.response.ExamResponseDto;

import java.util.List;

public interface ExamService {
    List<ExamResponseDto> getAllExams();
    ExamResponseDto getExamById(Long id);
    ExamResponseDto createExam(ExamRequestDto request, Long currentUserId);
    ExamResponseDto updateExam(Long id, ExamRequestDto request);
    void deleteExam(Long id);
}
