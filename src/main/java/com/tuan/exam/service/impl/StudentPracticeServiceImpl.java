package com.tuan.exam.service.impl;

import com.tuan.exam.entity.*;
import com.tuan.exam.entity.enums.AttemptStatus;
import com.tuan.exam.repository.QuizAttemptRepository;
import com.tuan.exam.repository.QuizRepository;
import com.tuan.exam.repository.SubjectRepository;
import com.tuan.exam.repository.UserRepository;
import com.tuan.exam.service.interfaces.StudentPracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentPracticeServiceImpl implements StudentPracticeService {

    private final SubjectRepository subjectRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;

    // 1. Lấy dữ liệu cho các ô dropdown bộ lọc
    @Override
    public Map<String, Object> getFilterCategories() {
        List<Subject> subjects = subjectRepository.findAll();

        List<Map<String, Object>> subjectList = subjects.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getName());
            return map;
        }).collect(Collectors.toList());

        List<String> difficulties = List.of("Dễ", "Trung bình", "Khó");

        return Map.of(
                "subjects", subjectList,
                "difficulties", difficulties
        );
    }

    // 2. Tra cứu danh sách bài luyện tập
    @Override
    public List<Map<String, Object>> getPracticeQuizzes(Long subjectId, Long topicId, String difficulty, String sortBy, String search) {

        // Nếu client gửi 'ALL' hoặc chuỗi trống thì gán về null để câu lệnh JPQL ở trên nhận diện đúng
        if ("ALL".equalsIgnoreCase(difficulty) || (difficulty != null && difficulty.trim().isEmpty())) {
            difficulty = null;
        }
        if (search != null && search.trim().isEmpty()) {
            search = null;
        }

        List<Quiz> quizzes = quizRepository.filterPracticeQuizzes(subjectId, difficulty, search);

        return quizzes.stream().map(quiz -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", quiz.getId());
            map.put("title", quiz.getTitle());
            map.put("code", quiz.getCode());
            map.put("subjectName", quiz.getSubject() != null ? quiz.getSubject().getName() : "Chung");

            QuizSettings settings = quiz.getSettings();
            map.put("durationMinutes", (settings != null && settings.getDurationMinutes() != null) ? settings.getDurationMinutes() : 45);
            map.put("totalQuestions", quiz.getTotalQuestions() != null ? quiz.getTotalQuestions() : 0);
            map.put("difficulty", (settings != null && settings.getDifficulty() != null) ? settings.getDifficulty() : "Trung bình");
            return map;
        }).collect(Collectors.toList());
    }

    // 3. Khởi tạo lượt làm bài thi (Tạo QuizAttempt với status IN_PROGRESS)
    @Override
    @Transactional
    public Map<String, Object> startPractice(Long quizId, String username) {
        // 🟢 SỬA ĐÚNG TẠI ĐÂY: Đổi findByEmail thành findByEmailOrUsername để khớp username "trang1234"
        User user = userRepository.findByEmailOrUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh: " + username));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi với ID: " + quizId));

        // Kiểm tra xem đã có lượt thi nào đang IN_PROGRESS chưa
        Optional<QuizAttempt> existingAttempt = quizAttemptRepository
                .findByUserIdAndQuizIdAndStatus(user.getId(), quiz.getId(), AttemptStatus.IN_PROGRESS);

        if (existingAttempt.isPresent()) {
            // Nếu đã có bài làm dở, trả về attemptId cũ để học sinh làm tiếp
            return Map.of(
                    "attemptId", existingAttempt.get().getId(),
                    "message", "Tiếp tục bài thi đang làm dở"
            );
        }

        // Tính số lần thi đã thực hiện
        long attemptCount = quizAttemptRepository.countByUserIdAndQuizId(user.getId(), quiz.getId());

        // Tạo lượt làm bài mới
        QuizAttempt newAttempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .attemptNumber((int) attemptCount + 1)
                .startTime(Instant.now())
                .status(AttemptStatus.IN_PROGRESS)
                .score(0.0)
                .violationCount(0)
                .isForceSubmitted(false)
                .build();

        QuizAttempt savedAttempt = quizAttemptRepository.save(newAttempt);

        return Map.of(
                "attemptId", savedAttempt.getId(),
                "message", "Bắt đầu bài thi thành công"
        );
    }
}