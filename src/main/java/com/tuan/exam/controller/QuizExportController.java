package com.tuan.exam.controller;

import com.tuan.exam.service.QuizExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizExportController {
    private final QuizExportService quizExportService;

    @GetMapping(
            value = "/{quizId}/export-scores",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<StreamingResponseBody> exportScores(@PathVariable Long quizId) {

        // Spring tự động cấp 1 OutputStream kết nối thẳng tới Trình duyệt của User
        StreamingResponseBody stream = outputStream -> {
            quizExportService.exportScoresToExcelLargeScale(quizId, outputStream);
        };

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=bang_diem_quiz_" + quizId + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(stream);
    }
}
