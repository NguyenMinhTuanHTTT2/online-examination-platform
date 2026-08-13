package com.tuan.exam.controller;

import com.tuan.exam.dto.request.QuizRequest;
import com.tuan.exam.dto.response.QuizResponse;
import com.tuan.exam.service.interfaces.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    // Phải có quyền TEACHER hoặc ADMIN mới được tạo đề thi
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<QuizResponse> createQuiz(
            @Valid @RequestBody QuizRequest request,
            Principal principal) {

        QuizResponse response = quizService.createQuiz(request, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<QuizResponse> updateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody QuizRequest request,
            Principal principal) {

        QuizResponse response = quizService.updateQuiz(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<QuizResponse> publishQuiz(
            @PathVariable Long id,
            Principal principal) {

        QuizResponse response = quizService.publishQuiz(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse> getQuizById(@PathVariable Long id) {
        // API này ai đã login (Authenticated) cũng xem được chi tiết đề (nhưng ko có câu hỏi)
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    @GetMapping("/my-quizzes")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Page<QuizResponse>> getMyQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Principal principal) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<QuizResponse> responses = quizService.getMyQuizzes(principal.getName(), pageable);
        return ResponseEntity.ok(responses);
    }
}
