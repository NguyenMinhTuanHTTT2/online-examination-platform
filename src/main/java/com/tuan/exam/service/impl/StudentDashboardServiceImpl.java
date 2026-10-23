package com.tuan.exam.service.impl;

import com.tuan.exam.entity.Exam;
import com.tuan.exam.entity.Quiz;
import com.tuan.exam.entity.QuizAttempt;
import com.tuan.exam.entity.User;
import com.tuan.exam.entity.enums.AttemptStatus;
import com.tuan.exam.repository.ExamRepository;
import com.tuan.exam.repository.QuizAttemptRepository;
import com.tuan.exam.repository.UserRepository;
import com.tuan.exam.service.interfaces.StudentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentDashboardServiceImpl implements StudentDashboardService {

    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final ExamRepository examRepository;

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
    }

    // 1. Lấy chỉ số thống kê (4 ô chỉ số)
    @Override
    public Map<String, Object> getStudentStats(String username) {
        User user = getUserByUsername(username);

        // Lấy danh sách lượt thi đã hoàn thành (COMPLETED) của đúng user đó
        List<QuizAttempt> completedAttempts = quizAttemptRepository.findByUserIdAndStatus(user.getId(), AttemptStatus.COMPLETED);

        long totalExamsDone = completedAttempts.size();

        // Điểm trung bình
        double avgScore = completedAttempts.stream()
                .mapToDouble(a -> a.getScore() != null ? a.getScore() : 0.0)
                .average()
                .orElse(0.0);

        // Tổng thời gian làm bài (tính từ startTime đến submitTime)
        long totalSeconds = completedAttempts.stream()
                .mapToLong(attempt -> {
                    if (attempt.getStartTime() != null && attempt.getSubmitTime() != null) {
                        return Duration.between(attempt.getStartTime(), attempt.getSubmitTime()).getSeconds();
                    }
                    return 0L;
                })
                .sum();
        double totalStudyHours = Math.round((totalSeconds / 3600.0) * 10.0) / 10.0;

        // Tỷ lệ làm đúng (%) - Quy đổi từ điểm số hệ 10
        double accuracyRate = avgScore * 10.0;

        return Map.of(
                "totalExamsDone", totalExamsDone,
                "avgScore", Math.round(avgScore * 10.0) / 10.0,
                "totalStudyHours", totalStudyHours,
                "accuracyRate", Math.round(accuracyRate * 10.0) / 10.0
        );
    }

    // 2. Biểu đồ phân bố điểm số
    @Override
    public List<Map<String, Object>> getScoreDistribution(String username) {
        User user = getUserByUsername(username);
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdAndStatus(user.getId(), AttemptStatus.COMPLETED);

        long count9To10 = attempts.stream().filter(a -> a.getScore() != null && a.getScore() >= 9.0).count();
        long count7To89 = attempts.stream().filter(a -> a.getScore() != null && a.getScore() >= 7.0 && a.getScore() < 9.0).count();
        long count5To69 = attempts.stream().filter(a -> a.getScore() != null && a.getScore() >= 5.0 && a.getScore() < 7.0).count();
        long count3To49 = attempts.stream().filter(a -> a.getScore() != null && a.getScore() >= 3.0 && a.getScore() < 5.0).count();
        long count0To29 = attempts.stream().filter(a -> a.getScore() != null && a.getScore() < 3.0).count();

        long total = Math.max(1, attempts.size());

        return List.of(
                Map.of("label", "Xuất sắc (9 - 10)", "percentage", Math.round((count9To10 * 100.0 / total) * 10.0) / 10.0, "count", count9To10, "color", "#22c55e"),
                Map.of("label", "Giỏi (7 - 8.9)", "percentage", Math.round((count7To89 * 100.0 / total) * 10.0) / 10.0, "count", count7To89, "color", "#3b82f6"),
                Map.of("label", "Khá (5 - 6.9)", "percentage", Math.round((count5To69 * 100.0 / total) * 10.0) / 10.0, "count", count5To69, "color", "#eab308"),
                Map.of("label", "Trung bình (3 - 4.9)", "percentage", Math.round((count3To49 * 100.0 / total) * 10.0) / 10.0, "count", count3To49, "color", "#f97316"),
                Map.of("label", "Yếu (0 - 2.9)", "percentage", Math.round((count0To29 * 100.0 / total) * 10.0) / 10.0, "count", count0To29, "color", "#ef4444")
        );
    }

    // 3. Đề thi nổi bật (Lấy kỳ thi đang mở)
    @Override
    public Map<String, Object> getFeaturedExam() {
        List<Exam> openExams = examRepository.findByStatus("Đang mở");
        if (openExams.isEmpty()) {
            openExams = examRepository.findAll();
            if (openExams.isEmpty()) return Map.of();
        }

        Exam exam = openExams.get(0);
        return Map.of(
                "id", exam.getId(),
                "title", exam.getTitle() != null ? exam.getTitle() : "Đề thi tổng hợp",
                "subject", "Toán",
                "grade", "Khối 12",
                "durationMinutes", 90,
                "totalQuestions", 50,
                "participantsCount", 120,
                "status", exam.getStatus() != null ? exam.getStatus() : "Đang mở",
                "endTime", ""
        );
    }

    // 4. Danh sách các bài thi đang làm dở (IN_PROGRESS)
    @Override
    public List<Map<String, Object>> getPendingExams(String username) {
        User user = getUserByUsername(username);

        List<QuizAttempt> pendingAttempts = quizAttemptRepository.findByUserIdAndStatus(user.getId(), AttemptStatus.IN_PROGRESS);

        return pendingAttempts.stream().map(attempt -> {
            Quiz quiz = attempt.getQuiz();
            int completed = attempt.getUserAnswers() != null ? attempt.getUserAnswers().size() : 0;
            int total = attempt.getAttemptQuestions() != null && !attempt.getAttemptQuestions().isEmpty()
                    ? attempt.getAttemptQuestions().size() : 1;
            int progress = (completed * 100) / total;

            Map<String, Object> map = new HashMap<>();
            map.put("attemptId", attempt.getId());
            map.put("quizId", quiz != null ? quiz.getId() : null);
            map.put("title", quiz != null ? quiz.getTitle() : "Bài thi trắc nghiệm");
            map.put("completedQuestions", completed);
            map.put("totalQuestions", total);
            map.put("progressPercentage", progress);
            map.put("subject", "Môn học");
            return map;
        }).collect(Collectors.toList());
    }

    // 5. Danh sách đề thi gợi ý
    @Override
    public List<Map<String, Object>> getSuggestedExams(String username) {
        List<Exam> exams = examRepository.findAll();
        if (exams.size() > 3) {
            exams = exams.subList(0, 3);
        }

        return exams.stream().map(exam -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", exam.getId());
            map.put("title", exam.getTitle());
            map.put("subject", "Môn học");
            map.put("grade", "Khối 12");
            map.put("durationMinutes", 45);
            map.put("totalQuestions", 30);
            map.put("difficulty", "Trung bình");
            return map;
        }).collect(Collectors.toList());
    }

    // 6. Dữ liệu tiến độ học tập (Biểu đồ đường)
    @Override
    public List<Map<String, Object>> getLearningProgress(String username, String range) {
        User user = getUserByUsername(username);

        List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdAndStatusOrderBySubmitTimeDesc(user.getId(), AttemptStatus.COMPLETED);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM").withZone(ZoneId.systemDefault());

        return attempts.stream().limit(7).map(attempt -> {
            Map<String, Object> map = new HashMap<>();
            String dateStr = attempt.getSubmitTime() != null ? formatter.format(attempt.getSubmitTime()) : "--/--";
            map.put("date", dateStr);
            map.put("val", attempt.getScore() != null ? attempt.getScore() : 0.0);
            return map;
        }).collect(Collectors.toList());
    }
}