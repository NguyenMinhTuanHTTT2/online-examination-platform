package com.tuan.exam.controller;

import com.tuan.exam.service.interfaces.StudentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/student/dashboard")
@RequiredArgsConstructor
public class StudentDashboardController {
    private final StudentDashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(Authentication authentication) {
        System.out.println("DEBUG - Username Spring Security đang lấy là: " + authentication.getName());
        return ResponseEntity.ok(dashboardService.getStudentStats(authentication.getName()));
    }

    @GetMapping("/score-distribution")
    public ResponseEntity<?> getScoreDistribution(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getScoreDistribution(authentication.getName()));
    }

    @GetMapping("/featured-exam")
    public ResponseEntity<?> getFeaturedExam() {
        return ResponseEntity.ok(dashboardService.getFeaturedExam());
    }

    @GetMapping("/pending-exams")
    public ResponseEntity<?> getPendingExams(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getPendingExams(authentication.getName()));
    }

    @GetMapping("/suggested-exams")
    public ResponseEntity<?> getSuggestedExams(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getSuggestedExams(authentication.getName()));
    }

    @GetMapping("/progress")
    public ResponseEntity<?> getProgress(
            Authentication authentication,
            @RequestParam(defaultValue = "7 ngày") String range) {
        return ResponseEntity.ok(dashboardService.getLearningProgress(authentication.getName(), range));
    }
}
