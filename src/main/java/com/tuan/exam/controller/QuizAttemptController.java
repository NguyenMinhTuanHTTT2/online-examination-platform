package com.tuan.exam.controller;

import com.tuan.exam.dto.request.SubmitQuizRequest;
import com.tuan.exam.dto.response.QuizResultResponse;
import com.tuan.exam.dto.response.QuizTakeResponse;
import com.tuan.exam.service.interfaces.QuizAttemptService;
import com.tuan.exam.service.interfaces.QuizService;
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
    private final QuizService quizService;

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

    // 4. API Lưu nháp
    @PatchMapping("/{attemptId}/auto-save")
    public ResponseEntity<Void> autoSave(
            @PathVariable Long attemptId,
            @RequestBody SubmitQuizRequest request,
            Principal principal) {
        if (request != null) {
            attemptService.autoSave(attemptId, request, principal.getName());
        }
        return ResponseEntity.ok().build();
    }

    // 5. API Cảnh báo gian lận
    @PostMapping("/{attemptId}/violations")
    public ResponseEntity<QuizResultResponse> reportViolation(
            @PathVariable Long attemptId,
            Principal principal) {
        QuizResultResponse result = attemptService.reportViolation(attemptId, principal.getName());
        if (result != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }
        return ResponseEntity.ok().build();
    }

    // 6. Lấy đề thi đã trộn (GIỮ NGUYÊN GỌI quizService KÈM QuizTakeResponse)
    @GetMapping("/{attemptId}/take")
    public ResponseEntity<QuizTakeResponse> getQuizTakeData(
            @PathVariable Long attemptId,
            Principal principal) {
        QuizTakeResponse response = quizService.getQuizTakeData(attemptId, principal.getName());
        return ResponseEntity.ok(response);
    }
}