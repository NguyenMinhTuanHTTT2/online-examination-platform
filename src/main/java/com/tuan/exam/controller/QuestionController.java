package com.tuan.exam.controller;

import com.tuan.exam.dto.request.QuestionRequest;
import com.tuan.exam.dto.response.QuestionResponse;
import com.tuan.exam.service.interfaces.QuestionImportService;
import com.tuan.exam.service.interfaces.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;
    private final QuestionImportService questionImportService;

    // 1. Lấy danh sách câu hỏi của 1 đề thi
    @GetMapping("/{quizId}/questions")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByQuizId(
            @PathVariable Long quizId,
            Principal principal) {

        List<QuestionResponse> responses = questionService.getQuestionsByQuizId(quizId, principal.getName());
        return ResponseEntity.ok(responses);
    }

    // 2. Thêm câu hỏi mới vào đề thi
    @PostMapping("/{quizId}/questions")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<QuestionResponse> addQuestionToQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuestionRequest request,
            Principal principal) {

        QuestionResponse response = questionService.addQuestionToQuiz(quizId, request, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 3. Cập nhật câu hỏi
    @PutMapping("/{quizId}/questions/{questionId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long quizId,
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionRequest request,
            Principal principal) {

        QuestionResponse response = questionService.updateQuestion(quizId, questionId, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    // 4. Xóa câu hỏi theo ID
    @DeleteMapping("/{quizId}/questions/{questionId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long quizId,
            @PathVariable Long questionId,
            Principal principal) {

        questionService.deleteQuestion(quizId, questionId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    // 5. Import câu hỏi từ file Excel (Đã chuyển từ Service sang đúng vị trí Controller)
    @PostMapping("/{quizId}/questions/import")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> importQuestions(
            @PathVariable Long quizId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            questionImportService.importQuestionsFromExcel(quizId, file);
            return ResponseEntity.ok("Import danh sách câu hỏi thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}