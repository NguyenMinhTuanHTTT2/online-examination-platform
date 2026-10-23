package com.tuan.exam.controller;

import com.tuan.exam.service.impl.ExcelExportServiceImpl;
import com.tuan.exam.service.interfaces.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.security.Principal;

@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
public class ExcelExportController {
    private final ExcelExportService excelExportService;

    @GetMapping("/quizzes/{quizId}/scores")
    public ResponseEntity<InputStreamResource> exportQuizScores(
            @PathVariable Long quizId,
            Principal principal // Lấy username của giảng viên đang đăng nhập
    ) {
        String username = principal.getName();
        ByteArrayInputStream in = excelExportService.exportQuizScoresToExcel(quizId, username);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=bang-diem-quiz-" + quizId + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
