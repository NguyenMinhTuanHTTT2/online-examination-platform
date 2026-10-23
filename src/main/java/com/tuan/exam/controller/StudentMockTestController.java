package com.tuan.exam.controller;

import com.tuan.exam.dto.response.MockTestResponse;
import com.tuan.exam.service.interfaces.MockTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student/mock-tests")
@RequiredArgsConstructor
public class StudentMockTestController {
    private final MockTestService mockTestService;

    // API 1: Lấy danh sách đề thi thử (Hỗ trợ tìm kiếm, lọc theo môn học và trạng thái tab)
    @GetMapping
    public ResponseEntity<List<MockTestResponse>> getMockTests(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<MockTestResponse> mockTests = mockTestService.getStudentMockTests(
                search, subjectId, status, userDetails.getUsername()
        );
        return ResponseEntity.ok(mockTests);
    }

    // API 2: Bắt đầu hoặc tiếp tục một bài thi thử (trả về attemptId để chuyển hướng sang phòng thi)
    @PostMapping("/{quizId}/start")
    public ResponseEntity<Map<String, Object>> startMockTest(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = mockTestService.startMockTest(quizId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
