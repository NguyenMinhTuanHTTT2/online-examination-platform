package com.tuan.exam.service.impl;

import com.tuan.exam.dto.response.MockTestResponse;
import com.tuan.exam.entity.Quiz;
import com.tuan.exam.entity.QuizAttempt;
import com.tuan.exam.entity.User;
import com.tuan.exam.entity.enums.AttemptStatus;
import com.tuan.exam.repository.QuestionRepository;
import com.tuan.exam.repository.QuizAttemptRepository;
import com.tuan.exam.repository.QuizRepository;
import com.tuan.exam.repository.UserRepository;
import com.tuan.exam.service.interfaces.MockTestService;
import com.tuan.exam.service.interfaces.QuizAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;


@Service
@RequiredArgsConstructor
public class MockTestServiceImpl implements MockTestService {
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptService quizAttemptService;

    @Override
    @Transactional(readOnly = true)
    public List<MockTestResponse> getStudentMockTests(String search, Long subjectId, String status, String username) {
        // 1. Xác thực học viên từ Database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // 2. Lấy danh sách các đề thi thử (MOCK_TEST) từ Database thông qua query filter
        List<Quiz> quizzes = quizRepository.filterMockTestQuizzes(subjectId, search);

        List<MockTestResponse> responseList = new ArrayList<>();

        for (Quiz quiz : quizzes) {
            // 3. Truy vấn tất cả lượt thi của học viên cho đề thi này từ CSDL
            List<QuizAttempt> userAttempts = attemptRepository.findByUserIdAndQuizId(user.getId(), quiz.getId());

            String mockStatus = "NOT_STARTED";
            Long lastAttemptId = null;
            Double lastScore = null;

            if (userAttempts != null && !userAttempts.isEmpty()) {
                // Sắp xếp các lượt thi giảm dần theo thời gian bắt đầu (mới nhất lên đầu)
                userAttempts.sort((a, b) -> {
                    if (a.getStartTime() == null || b.getStartTime() == null) return 0;
                    return b.getStartTime().compareTo(a.getStartTime());
                });

                QuizAttempt latestAttempt = userAttempts.get(0);
                lastAttemptId = latestAttempt.getId();
                lastScore = latestAttempt.getScore();

                // Kiểm tra xem có lượt nào đang làm dở (IN_PROGRESS) hay đã hoàn thành (SUBMITTED)
                boolean hasInProgress = userAttempts.stream()
                        .anyMatch(a -> a.getStatus() == AttemptStatus.IN_PROGRESS);
                boolean hasSubmitted = userAttempts.stream()
                        .anyMatch(a -> a.getStatus() == AttemptStatus.SUBMITTED);

                if (hasInProgress) {
                    mockStatus = "IN_PROGRESS";
                } else if (hasSubmitted) {
                    mockStatus = "COMPLETED";
                }
            }

            // 4. Lọc theo Tab Status yêu cầu từ phía Client (ALL, NOT_STARTED, IN_PROGRESS, COMPLETED)
            if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
                if (!status.equalsIgnoreCase(mockStatus)) {
                    continue; // Bỏ qua nếu không khớp trạng thái tab đang chọn
                }
            }

            // 5. Đếm tổng số câu hỏi thực tế từ CSDL
            int totalQuestions = (int) questionRepository.countByQuizId(quiz.getId());
            if (totalQuestions == 0 && quiz.getQuestions() != null) {
                totalQuestions = quiz.getQuestions().size();
            }

            // 6. Lấy thời gian làm bài từ QuizSettings (mặc định 45 phút nếu null)
            Integer durationMinutes = (quiz.getSettings() != null && quiz.getSettings().getDurationMinutes() != null)
                    ? quiz.getSettings().getDurationMinutes() : 45;

            // 7. Thông tin môn học từ quan hệ CSDL
            Long subId = quiz.getSubject() != null ? quiz.getSubject().getId() : null;
            String subjectName = quiz.getSubject() != null ? quiz.getSubject().getName() : "Chung";

            responseList.add(MockTestResponse.builder()
                    .id(quiz.getId())
                    .title(quiz.getTitle())
                    .description(quiz.getDescription())
                    .subjectId(subId)
                    .subjectName(subjectName)
                    .durationMinutes(durationMinutes)
                    .totalQuestions(totalQuestions)
                    .status(mockStatus)
                    .lastAttemptId(lastAttemptId)
                    .lastScore(lastScore)
                    .createdAt(quiz.getCreatedAt() != null
                            ? quiz.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()
                            : java.time.Instant.now())
                    .build());
        }

        return responseList;
    }

    @Override
    @Transactional
    public Map<String, Object> startMockTest(Long quizId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Kiểm tra xem học viên có đang làm dở bài thi này hay không
        List<QuizAttempt> existingAttempts = attemptRepository.findByUserIdAndQuizId(user.getId(), quizId);
        Optional<QuizAttempt> inProgressAttempt = existingAttempts.stream()
                .filter(a -> a.getStatus() == AttemptStatus.IN_PROGRESS)
                .findFirst();

        Long attemptId;
        if (inProgressAttempt.isPresent()) {
            // Nếu có bài đang làm dở, trả về attemptId cũ để học viên tiếp tục làm
            attemptId = inProgressAttempt.get().getId();
        } else {
            // Nếu chưa có, gọi service khởi tạo lượt thi mới từ CSDL
            attemptId = quizAttemptService.startQuizAttempt(quizId, username);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("attemptId", attemptId);
        response.put("message", "Khởi tạo hoặc tiếp tục bài thi thử thành công");
        return response;
    }
}
