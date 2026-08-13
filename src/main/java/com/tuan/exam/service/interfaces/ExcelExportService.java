package com.tuan.exam.service.interfaces;

import java.io.ByteArrayInputStream;

public interface ExcelExportService {
    ByteArrayInputStream exportQuizScoresToExcel(Long quizId, String username);
}
