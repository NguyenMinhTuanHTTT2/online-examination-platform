package com.tuan.exam.controller;

import com.tuan.exam.service.interfaces.StudentPracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/student/practice") // 🟢 SỬA ĐÚNG TẠI ĐÂY: Thêm /api/v1 khớp với Axios ở React
@RequiredArgsConstructor
public class StudentPracticeController {

    private final StudentPracticeService practiceService;

    // 1. Lấy danh mục cho bộ lọc (Môn học, Mức độ)
    @GetMapping("/categories")
    public ResponseEntity<?> getFilterCategories() {
        return ResponseEntity.ok(practiceService.getFilterCategories());
    }

    // 2. Lấy danh sách đề luyện tập theo bộ lọc & từ khóa
    @GetMapping("/quizzes")
    public ResponseEntity<?> getPracticeQuizzes(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(practiceService.getPracticeQuizzes(subjectId, topicId, difficulty, sortBy, search));
    }

    // 3. Khởi tạo lượt làm bài tập tự do
    @PostMapping("/start")
    public ResponseEntity<?> startPractice(
            @RequestBody Map<String, Long> requestBody,
            Authentication authentication) {
        Long quizId = requestBody.get("quizId");
        String username = authentication.getName();
        return ResponseEntity.ok(practiceService.startPractice(quizId, username));
    }

}