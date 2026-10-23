package com.tuan.exam.controller;

import com.tuan.exam.dto.request.ExamRequestDto;
import com.tuan.exam.dto.response.ExamResponseDto;
import com.tuan.exam.service.interfaces.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {
    private final ExamService examService;

    // Lấy danh sách toàn bộ kỳ thi
    @GetMapping
    public ResponseEntity<List<ExamResponseDto>> getAllExams() {
        List<ExamResponseDto> exams = examService.getAllExams();
        return ResponseEntity.ok(exams);
    }

    // Lấy chi tiết 1 kỳ thi theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ExamResponseDto> getExamById(@PathVariable Long id) {
        ExamResponseDto exam = examService.getExamById(id);
        return ResponseEntity.ok(exam);
    }

    // Tạo mới kỳ thi (Giáo viên bấm tạo)
    @PostMapping
    public ResponseEntity<ExamResponseDto> createExam(
            @Valid @RequestBody ExamRequestDto request,
            @RequestParam Long currentUserId // Nhận ID giáo viên đang thao tác
    ) {
        ExamResponseDto createdExam = examService.createExam(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdExam);
    }

    // Cập nhật thông tin kỳ thi
    @PutMapping("/{id}")
    public ResponseEntity<ExamResponseDto> updateExam(
            @PathVariable Long id,
            @Valid @RequestBody ExamRequestDto request
    ) {
        ExamResponseDto updatedExam = examService.updateExam(id, request);
        return ResponseEntity.ok(updatedExam);
    }

    // Xóa kỳ thi
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }
}
