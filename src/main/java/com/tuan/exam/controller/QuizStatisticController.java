package com.tuan.exam.controller;

import com.tuan.exam.dto.response.QuizStatisticResponse;
import com.tuan.exam.service.interfaces.ExcelExportService;
import com.tuan.exam.service.interfaces.QuizStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.security.Principal;

@RestController
@RequestMapping("quizzes")
@RequiredArgsConstructor
public class QuizStatisticController {

    private final QuizStatisticService statisticService;
    private final ExcelExportService excelExportService;

    @GetMapping("/{quizId}/statistics")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<QuizStatisticResponse> getQuizStatistics(
            @PathVariable Long quizId,
            Principal principal) {

        QuizStatisticResponse response = statisticService.getQuizStatistics(quizId, principal.getName());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{quizId}/export-scores")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Resource> exportQuizScores(
            @PathVariable Long quizId,
            Principal principal) {

        // Gọi Service sinh file Excel
        ByteArrayInputStream stream = excelExportService.exportQuizScoresToExcel(quizId, principal.getName());
        InputStreamResource file = new InputStreamResource(stream);

        // Đặt tên file sẽ tải xuống
        String filename = "BangDiem_Quiz_" + quizId + ".xlsx";

        return ResponseEntity.ok()
                // Header báo cho trình duyệt biết đây là file tải về
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                // Định dạng Content-Type chuẩn của file Excel (.xlsx)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}
