package com.tuan.exam.service.impl;

import com.tuan.exam.dto.QuizSettingsDto;
import com.tuan.exam.dto.request.QuizRequest;
import com.tuan.exam.dto.response.QuizResponse;
import com.tuan.exam.entity.Quiz;
import com.tuan.exam.entity.QuizSettings;
import com.tuan.exam.entity.User;
import com.tuan.exam.entity.enums.QuizStatus;
import com.tuan.exam.repository.QuizRepository;
import com.tuan.exam.repository.UserRepository;
import com.tuan.exam.service.interfaces.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public QuizResponse createQuiz(QuizRequest request, String username) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Nếu người dùng không truyền Code phòng thi, tự động sinh mã 8 ký tự
        String quizCode = (request.getCode() != null && !request.getCode().isBlank())
                ? request.getCode()
                : UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Map thông tin Quiz cơ bản
        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .code(quizCode)
                .creator(creator)
                .status(QuizStatus.DRAFT) // Mặc định là bản nháp
                .build();

        // Map thông tin Settings
        QuizSettingsDto sDto = request.getSettings();
        QuizSettings settings = QuizSettings.builder()
                .quiz(quiz) // Bắt buộc set reference để JPA lưu khoá ngoại (MapsId)
                .durationMinutes(sDto.getDurationMinutes())
                .passScorePercentage(sDto.getPassScorePercentage() != null ? sDto.getPassScorePercentage() : 50)
                .maxAttempts(sDto.getMaxAttempts() != null ? sDto.getMaxAttempts() : 1)
                .shuffleQuestions(sDto.getShuffleQuestions() != null ? sDto.getShuffleQuestions() : false)
                .shuffleAnswers(sDto.getShuffleAnswers() != null ? sDto.getShuffleAnswers() : false)
                .showResultImmediately(sDto.getShowResultImmediately() != null ? sDto.getShowResultImmediately() : true)
                .allowReview(sDto.getAllowReview() != null ? sDto.getAllowReview() : true)
                .startTime(sDto.getStartTime())
                .endTime(sDto.getEndTime())
                .build();

        quiz.setSettings(settings); // Link 2 chiều

        Quiz savedQuiz = quizRepository.save(quiz); // Do có CascadeType.ALL, Settings sẽ tự động được lưu
        return mapToResponse(savedQuiz);
    }

    @Override
    @Transactional
    public QuizResponse updateQuiz(Long id, QuizRequest request, String username) {
        Quiz quiz = getQuizAndCheckOwnership(id, username);

        if (quiz.getStatus() == QuizStatus.ARCHIVED) {
            throw new RuntimeException("Không thể sửa đề thi đã bị lưu trữ!");
        }

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        if (request.getCode() != null && !request.getCode().isBlank()) {
            quiz.setCode(request.getCode());
        }

        // Cập nhật Settings
        QuizSettings settings = quiz.getSettings();
        QuizSettingsDto sDto = request.getSettings();

        settings.setDurationMinutes(sDto.getDurationMinutes());
        settings.setPassScorePercentage(sDto.getPassScorePercentage());
        settings.setMaxAttempts(sDto.getMaxAttempts());
        settings.setShuffleQuestions(sDto.getShuffleQuestions());
        settings.setShuffleAnswers(sDto.getShuffleAnswers());
        settings.setShowResultImmediately(sDto.getShowResultImmediately());
        settings.setAllowReview(sDto.getAllowReview());
        settings.setStartTime(sDto.getStartTime());
        settings.setEndTime(sDto.getEndTime());

        return mapToResponse(quizRepository.save(quiz));
    }

    @Override
    @Transactional
    public QuizResponse publishQuiz(Long id, String username) {
        Quiz quiz = getQuizAndCheckOwnership(id, username);

        // Cần đảm bảo đề thi có ít nhất 1 câu hỏi mới cho xuất bản (Tùy logic doanh nghiệp)
        // if (quiz.getTotalQuestions() == 0) throw new RuntimeException("Đề thi chưa có câu hỏi!");

        quiz.setStatus(QuizStatus.PUBLISHED);
        return mapToResponse(quizRepository.save(quiz));
    }

    @Override
    public QuizResponse getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi"));
        return mapToResponse(quiz);
    }

    @Override
    public Page<QuizResponse> getMyQuizzes(String username, Pageable pageable) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return quizRepository.findByCreatorId(creator.getId(), pageable)
                .map(this::mapToResponse);
    }

    // --- Helper Methods ---

    private Quiz getQuizAndCheckOwnership(Long quizId, String username) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi"));

        // Kiểm tra quyền sở hữu: Chỉ người tạo mới được sửa
        if (!quiz.getCreator().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền thực hiện hành động này trên đề thi của người khác");
        }
        return quiz;
    }

    private QuizResponse mapToResponse(Quiz quiz) {
        QuizSettings s = quiz.getSettings();
        QuizSettingsDto sDto = QuizSettingsDto.builder()
                .durationMinutes(s.getDurationMinutes())
                .passScorePercentage(s.getPassScorePercentage())
                .maxAttempts(s.getMaxAttempts())
                .shuffleQuestions(s.getShuffleQuestions())
                .shuffleAnswers(s.getShuffleAnswers())
                .showResultImmediately(s.getShowResultImmediately())
                .allowReview(s.getAllowReview())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .build();

        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .code(quiz.getCode())
                .status(quiz.getStatus())
                .totalQuestions(quiz.getTotalQuestions())
                .totalMarks(quiz.getTotalMarks())
                .creatorName(quiz.getCreator().getFullName())
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .settings(sDto)
                .build();
    }

}
