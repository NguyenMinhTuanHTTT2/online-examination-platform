package com.tuan.exam.controller;

import com.tuan.exam.dto.request.SubmitQuizRequest;
import com.tuan.exam.dto.response.QuizResultResponse;
import com.tuan.exam.service.interfaces.QuizAttemptService;

import jakarta.persistence.GeneratedValue;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/attempts")
@RequiredArgsConstructor
public class QuizAttemptController {
    private final QuizAttemptService attemptService;
    // 1. Học viên ấn "Bắt đầu làm bài"
    @PostMapping("/start/{quizId}")
    public ResponseEntity<Long> startQuiz(
            @PathVariable Long quizId,
            Principal principal) {

        Long attemptId = attemptService.startQuizAttempt(quizId, principal.getName());
        return new ResponseEntity<>(attemptId, HttpStatus.CREATED);
    }

    // 2. Học viên ấn "Nộp bài"
    @PostMapping("/{attemptId}/submit")
    public ResponseEntity<QuizResultResponse> submitQuiz(
            @PathVariable Long attemptId,
            @Valid @RequestBody SubmitQuizRequest request,
            Principal principal) {

        QuizResultResponse result = attemptService.submitQuizAttempt(attemptId, request, principal.getName());
        return ResponseEntity.ok(result);
    }

    // 3. Xem lại kết quả lượt thi cũ
    @GetMapping("/{attemptId}/result")
    public ResponseEntity<QuizResultResponse> getResult(
            @PathVariable Long attemptId,
            Principal principal) {

        QuizResultResponse result = attemptService.getAttemptResult(attemptId, principal.getName());
        return ResponseEntity.ok(result);
    }
    // 4. API Lưu nháp (Frontend gọi ngầm mỗi 30s/60s)
    @PatchMapping("/{attemptId}/auto-save")
    public ResponseEntity<Void> autoSave(
            @PathVariable Long attemptId,
            @RequestBody SubmitQuizRequest request,
            Principal principal) {
        attemptService.autoSave(attemptId, request, principal.getName());
        return ResponseEntity.ok().build();
    }

    // 5. API Cảnh báo gian lận (Frontend gọi khi Page Visibility bị ẩn)
    @PostMapping("/{attemptId}/violations")
    public ResponseEntity<QuizResultResponse> reportViolation(
            @PathVariable Long attemptId,
            Principal principal) {

        QuizResultResponse result = attemptService.reportViolation(attemptId, principal.getName());

        if (result != null) {
            // Nếu result khác null tức là đã bị hệ thống ép thu bài
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        // Chưa vượt quá số lần, chỉ ghi nhận
        return ResponseEntity.ok().build();
    }

}
