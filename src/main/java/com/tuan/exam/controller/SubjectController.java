package com.tuan.exam.controller;

import com.tuan.exam.dto.response.SubjectResponse;
import com.tuan.exam.entity.Subject;
import com.tuan.exam.service.interfaces.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    // Lấy danh sách toàn bộ môn học
    @GetMapping
    public ResponseEntity<List<SubjectResponse>> getAllSubjects() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }

    // Lấy thông tin chi tiết một môn học dựa vào mã môn (Ví dụ: /subjects/MATH)
    @GetMapping("/{code}")
    public ResponseEntity<SubjectResponse> getSubjectByCode(@PathVariable String code) {
        return ResponseEntity.ok(subjectService.getSubjectByCode(code));
    }

    // Thêm mới môn học (Dành cho Admin cấu hình hệ thống thi)
    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(@RequestBody Subject subject) {
        return ResponseEntity.ok(subjectService.createSubject(subject));
    }
}