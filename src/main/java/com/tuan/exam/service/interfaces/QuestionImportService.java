package com.tuan.exam.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface QuestionImportService {
    void importQuestionsFromExcel(Long quizId, MultipartFile file);
}
