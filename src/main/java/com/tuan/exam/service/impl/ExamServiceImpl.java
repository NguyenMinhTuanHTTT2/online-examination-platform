package com.tuan.exam.service.impl;

import com.tuan.exam.dto.request.ExamRequestDto;
import com.tuan.exam.dto.response.ExamResponseDto;
import com.tuan.exam.entity.Exam;
import com.tuan.exam.entity.Quiz;
import com.tuan.exam.entity.User;
import com.tuan.exam.repository.ExamRepository;
import com.tuan.exam.repository.QuizRepository;
import com.tuan.exam.repository.UserRepository;
import com.tuan.exam.service.interfaces.ExamService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {
    private final ExamRepository examRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ExamResponseDto> getAllExams() {
        return examRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ExamResponseDto getExamById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kỳ thi với ID: " + id));
        return mapToDto(exam);
    }

    @Override
    @Transactional
    public ExamResponseDto createExam(ExamRequestDto request, Long currentUserId) {
        // 1. Kiểm tra bộ đề (Quiz) có tồn tại không
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bộ đề (Quiz) với ID: " + request.getQuizId()));

        // 2. Kiểm tra giáo viên (User) tạo kỳ thi có tồn tại không
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + currentUserId));

        // 3. Khởi tạo đối tượng Exam từ request
        Exam exam = Exam.builder()
                .title(request.getTitle())
                .quiz(quiz)
                .creator(creator)
                .examType(request.getExamType() != null ? request.getExamType() : "Đề luyện tập")
                .durationMinutes(request.getDurationMinutes())
                .status(request.getStatus() != null ? request.getStatus() : "DRAFT")
                .build();

        Exam savedExam = examRepository.save(exam);
        return mapToDto(savedExam);
    }

    @Override
    @Transactional
    public ExamResponseDto updateExam(Long id, ExamRequestDto request) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kỳ thi với ID: " + id));

        if (request.getQuizId() != null && !request.getQuizId().equals(exam.getQuiz().getId())) {
            Quiz quiz = quizRepository.findById(request.getQuizId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bộ đề (Quiz) với ID: " + request.getQuizId()));
            exam.setQuiz(quiz);
        }

        exam.setTitle(request.getTitle());
        exam.setExamType(request.getExamType());
        exam.setDurationMinutes(request.getDurationMinutes());
        if (request.getStatus() != null) {
            exam.setStatus(request.getStatus());
        }

        Exam updatedExam = examRepository.save(exam);
        return mapToDto(updatedExam);
    }

    @Override
    @Transactional
    public void deleteExam(Long id) {
        if (!examRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy kỳ thi với ID: " + id);
        }
        examRepository.deleteById(id);
    }

    // Hàm chuyển đổi từ Entity sang DTO để hiển thị lên UI mượt mà
    private ExamResponseDto mapToDto(Exam exam) {
        String subjectName = "Chưa xác định";
        if (exam.getQuiz() != null && exam.getQuiz().getQuestions() != null && !exam.getQuiz().getQuestions().isEmpty()) {
            if (exam.getQuiz().getQuestions().get(0).getSubject() != null) {
                subjectName = exam.getQuiz().getQuestions().get(0).getSubject().getName();
            }
        }
        // Chuyển đổi LocalDateTime từ BaseEntity sang Instant cho DTO
        /*Instant createdAtInstant = null;
        if (exam.getCreatedAt() != null) {
            createdAtInstant = exam.getCreatedAt()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant();
        }*/

        return ExamResponseDto.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .quizId(exam.getQuiz() != null ? exam.getQuiz().getId() : null)
                .quizTitle(exam.getQuiz() != null ? exam.getQuiz().getTitle() : null)
                .subjectName(subjectName)
                .examType(exam.getExamType())
                .durationMinutes(exam.getDurationMinutes())
                .status(exam.getStatus())
                .attemptsCount(0L)  // Có thể cập nhật số liệu thống kê lượt làm bài sau
                .averageScore(0.0)   // Điểm trung bình tạm tính
                .createdAt(exam.getCreatedAt())
                .build();
    }
}
