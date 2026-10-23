package com.tuan.exam.controller;

import com.tuan.exam.entity.Quiz;
import com.tuan.exam.entity.QuizAttempt;
import com.tuan.exam.entity.enums.AttemptStatus;
import com.tuan.exam.entity.enums.QuizStatus;
import com.tuan.exam.repository.ExamRepository;
import com.tuan.exam.repository.QuestionRepository;
import com.tuan.exam.repository.QuizAttemptRepository;
import com.tuan.exam.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData(@RequestParam(name = "teacherId", defaultValue = "1") Long teacherId) {
        Map<String, Object> response = new HashMap<>();

        // 1. TRUY VẤN CÁC CON SỐ THỐNG KÊ BẰNG METHOD DỮ LIỆU CHUẨN
        long totalExams = quizRepository.countByCreatorId(teacherId);
        long totalQuestions = questionRepository.countByQuizCreatorId(teacherId);
        long activeExams = quizRepository.countByCreatorIdAndStatus(teacherId, QuizStatus.PUBLISHED);
        long totalAttempts = quizAttemptRepository.countByQuizCreatorIdAndStatus(teacherId, AttemptStatus.COMPLETED);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalExams", totalExams);
        stats.put("totalQuestions", totalQuestions);
        stats.put("activeExams", activeExams);
        stats.put("totalAttempts", totalAttempts);
        response.put("dashboardStats", stats);

        // 2. LẤY DANH SÁCH 5 ĐỀ THI MỚI NHẤT
        List<Quiz> quizzes = quizRepository.findTop5ByCreatorIdOrderByIdDesc(teacherId);
        List<Map<String, Object>> featuredExams = new ArrayList<>();

        for (Quiz quiz : quizzes) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", quiz.getId());
            item.put("title", quiz.getTitle());
            item.put("code", quiz.getCode());
            item.put("status", quiz.getStatus() != null ? quiz.getStatus().name() : "DRAFT");
            item.put("totalQuestions", quiz.getTotalQuestions());
            item.put("totalMarks", quiz.getTotalMarks());

            long attemptCount = quizAttemptRepository.countByUserIdAndQuizIdAndStatus(null, quiz.getId(), AttemptStatus.COMPLETED);
            item.put("participants", attemptCount);

            featuredExams.add(item);
        }
        response.put("featuredExams", featuredExams);

        // 3. LẤY 5 HOẠT ĐỘNG LÀM BÀI MỚI NHẤT
        List<QuizAttempt> recentAttempts = quizAttemptRepository.findTop5ByQuizCreatorIdAndStatusOrderByIdDesc(teacherId, AttemptStatus.COMPLETED);
        List<Map<String, Object>> recentActivities = new ArrayList<>();

        for (QuizAttempt attempt : recentAttempts) {
            Map<String, Object> act = new HashMap<>();
            act.put("id", attempt.getId());

            String studentName = (attempt.getUser() != null)
                    ? attempt.getUser().getUsername()
                    : "Học sinh #" + attempt.getId();

            act.put("studentName", studentName);
            act.put("quizTitle", attempt.getQuiz() != null ? attempt.getQuiz().getTitle() : "");
            act.put("score", attempt.getScore());
            act.put("submitTime", attempt.getSubmitTime());

            recentActivities.add(act);
        }
        response.put("recentActivities", recentActivities);

        return ResponseEntity.ok(response);
    }
}
