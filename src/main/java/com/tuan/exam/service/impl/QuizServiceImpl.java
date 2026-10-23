package com.tuan.exam.service.impl;

import com.tuan.exam.dto.QuizSettingsDto;
import com.tuan.exam.dto.request.QuizRequest;
import com.tuan.exam.dto.response.OptionDto;
import com.tuan.exam.dto.response.QuestionTakeDto;
import com.tuan.exam.dto.response.QuizResponse;
import com.tuan.exam.dto.response.QuizTakeResponse;
import com.tuan.exam.entity.*;
import com.tuan.exam.entity.enums.QuizStatus;
import com.tuan.exam.repository.*;
import com.tuan.exam.service.interfaces.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionRepository questionRepository;
    private final AttemptQuestionRepository attemptQuestionRepository;

    @Override
    @Transactional
    public QuizResponse createQuiz(QuizRequest request, String username) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        String quizCode = (request.getCode() != null && !request.getCode().isBlank())
                ? request.getCode()
                : UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .code(quizCode)
                .creator(creator)
                .status(QuizStatus.DRAFT)
                .quizType(request.getQuizType() != null ? request.getQuizType() : "MOCK_TEST") // 👈 Lưu loại đề
                .build();

        QuizSettingsDto sDto = request.getSettings();
        QuizSettings settings = QuizSettings.builder()
                .quiz(quiz)
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

        quiz.setSettings(settings);

        Quiz savedQuiz = quizRepository.save(quiz);
        return mapToResponse(savedQuiz);
    }

    @Override
    @Transactional
    public QuizResponse updateQuiz(Long id, QuizRequest request, String username) {
        Quiz quiz = getQuizAndCheckOwnership(id, username);

        // Đã gỡ bỏ điều kiện chặn "Không thể sửa đề thi đã ẩn!" để cho phép bật/tắt trạng thái linh hoạt
        quiz.setStatus(QuizStatus.valueOf(request.getStatus()));
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        if (request.getQuizType() != null) {
            quiz.setQuizType(request.getQuizType()); // 👈 Cập nhật loại đề khi sửa
        }
        if (request.getCode() != null && !request.getCode().isBlank()) {
            quiz.setCode(request.getCode());
        }

        QuizSettings settings = quiz.getSettings();
        QuizSettingsDto sDto = request.getSettings();

        // Kiểm tra an toàn tránh lỗi NullPointerException nếu request.getSettings() thiếu
        if (sDto != null) {
            settings.setDurationMinutes(sDto.getDurationMinutes());
            settings.setPassScorePercentage(sDto.getPassScorePercentage());
            settings.setMaxAttempts(sDto.getMaxAttempts());
            settings.setShuffleQuestions(sDto.getShuffleQuestions());
            settings.setShuffleAnswers(sDto.getShuffleAnswers());
            settings.setShowResultImmediately(sDto.getShowResultImmediately());
            settings.setAllowReview(sDto.getAllowReview());
            settings.setStartTime(sDto.getStartTime());
            settings.setEndTime(sDto.getEndTime());
        }

        return mapToResponse(quizRepository.save(quiz));
    }

    @Override
    @Transactional
    public QuizResponse publishQuiz(Long id, String username) {
        Quiz quiz = getQuizAndCheckOwnership(id, username);
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

    @Override
    public List<QuizResponse> getPublishedQuizzes() {
        Page<Quiz> publishedQuizzesPage = quizRepository.findByStatus(QuizStatus.PUBLISHED, Pageable.unpaged());

        return publishedQuizzesPage.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // LOGIC TẠO LƯỢT THI
    // =========================================================================
    @Override
    @Transactional
    public QuizAttempt startQuizAttempt(Long quizId, String username) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi"));

        if (quiz.getStatus() != QuizStatus.PUBLISHED) {
            throw new RuntimeException("Đề thi này chưa được xuất bản!");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(user);
        attempt.setStartTime(Instant.now());

        if (quiz.getSettings() != null && quiz.getSettings().getDurationMinutes() > 0) {
            Instant deadline = Instant.now().plus(Duration.ofMinutes(quiz.getSettings().getDurationMinutes()));
            attempt.setSubmitTime(deadline);
        }

        quizAttemptRepository.save(attempt);

        List<Question> selectedQuestions = new ArrayList<>();

        if (quiz.getRules() != null && !quiz.getRules().isEmpty()) {
            for (QuizRule rule : quiz.getRules()) {
                List<Question> easyQ = questionRepository.findRandomQuestions(rule.getSubject().getId(), "EASY", rule.getEasyCount());
                List<Question> mediumQ = questionRepository.findRandomQuestions(rule.getSubject().getId(), "MEDIUM", rule.getMediumCount());
                List<Question> hardQ = questionRepository.findRandomQuestions(rule.getSubject().getId(), "HARD", rule.getHardCount());

                selectedQuestions.addAll(easyQ);
                selectedQuestions.addAll(mediumQ);
                selectedQuestions.addAll(hardQ);
            }
        } else {
            List<Question> dbQuestions = questionRepository.findByQuizId(quizId);
            if (dbQuestions != null && !dbQuestions.isEmpty()) {
                selectedQuestions.addAll(dbQuestions);
            } else if (quiz.getQuestions() != null) {
                selectedQuestions.addAll(quiz.getQuestions());
            }
        }

        if (quiz.getSettings() != null && Boolean.TRUE.equals(quiz.getSettings().getShuffleQuestions())) {
            Collections.shuffle(selectedQuestions);
        }

        int order = 1;
        for (Question q : selectedQuestions) {
            List<Option> shuffledOptions = new ArrayList<>(q.getOptions());

            if (quiz.getSettings() != null && Boolean.TRUE.equals(quiz.getSettings().getShuffleAnswers())) {
                Collections.shuffle(shuffledOptions);
            }

            String optionOrderStr = shuffledOptions.stream()
                    .map(opt -> opt.getId().toString())
                    .collect(Collectors.joining(","));

            AttemptQuestion aq = new AttemptQuestion();
            aq.setAttempt(attempt);
            aq.setQuestion(q);
            aq.setQuestionOrder(order++);
            aq.setOptionOrder(optionOrderStr);

            attemptQuestionRepository.save(aq);
        }

        return attempt;
    }

    // =========================================================================
    // HÀM LẤY ĐỀ THI TRẢ VỀ FRONTEND
    // =========================================================================
    @Override
    @Transactional
    public QuizTakeResponse getQuizTakeData(Long attemptId, String username) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lượt làm bài"));

        if (!attempt.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền truy cập vào lượt làm bài này");
        }

        Quiz quiz = attempt.getQuiz();
        List<AttemptQuestion> aqList = attemptQuestionRepository.findByAttemptIdOrderByQuestionOrderAsc(attemptId);

        if (aqList.isEmpty()) {
            List<Question> dbQuestions = questionRepository.findByQuizId(quiz.getId());

            if ((dbQuestions == null || dbQuestions.isEmpty()) && quiz.getQuestions() != null) {
                dbQuestions = new ArrayList<>(quiz.getQuestions());
            }

            if (dbQuestions != null && !dbQuestions.isEmpty()) {
                int order = 1;
                for (Question q : dbQuestions) {
                    List<Option> options = new ArrayList<>(q.getOptions());
                    String optionOrderStr = options.stream()
                            .map(opt -> opt.getId().toString())
                            .collect(Collectors.joining(","));

                    AttemptQuestion aq = new AttemptQuestion();
                    aq.setAttempt(attempt);
                    aq.setQuestion(q);
                    aq.setQuestionOrder(order++);
                    aq.setOptionOrder(optionOrderStr);

                    attemptQuestionRepository.save(aq);
                    aqList.add(aq);
                }
            }
        }

        List<QuestionTakeDto> questionDtos = new ArrayList<>();

        for (AttemptQuestion aq : aqList) {
            Question question = aq.getQuestion();

            Map<Long, Option> optionMap = question.getOptions().stream()
                    .collect(Collectors.toMap(Option::getId, Function.identity()));

            List<OptionDto> optionDtos = new ArrayList<>();
            if (aq.getOptionOrder() != null && !aq.getOptionOrder().isBlank()) {
                String[] optionIds = aq.getOptionOrder().split(",");
                for (String optIdStr : optionIds) {
                    try {
                        Long optId = Long.parseLong(optIdStr.trim());
                        Option opt = optionMap.get(optId);
                        if (opt != null) {
                            optionDtos.add(OptionDto.builder()
                                    .id(opt.getId())
                                    .content(opt.getContent())
                                    .build());
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } else {
                for (Option opt : question.getOptions()) {
                    optionDtos.add(OptionDto.builder()
                            .id(opt.getId())
                            .content(opt.getContent())
                            .build());
                }
            }

            questionDtos.add(QuestionTakeDto.builder()
                    .questionOrder(aq.getQuestionOrder())
                    .questionId(question.getId())
                    .content(question.getContent())
                    .options(optionDtos)
                    .build());
        }

        return QuizTakeResponse.builder()
                .attemptId(attempt.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .durationMinutes(quiz.getSettings() != null ? quiz.getSettings().getDurationMinutes() : 0)
                .startTime(attempt.getStartTime())
                .endTime(attempt.getSubmitTime())
                .questions(questionDtos)
                .build();
    }

    // --- Helper Methods ---

    private Quiz getQuizAndCheckOwnership(Long quizId, String username) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi"));

        if (!quiz.getCreator().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền thực hiện hành động này trên đề thi của người khác");
        }
        return quiz;
    }

    private QuizResponse mapToResponse(Quiz quiz) {
        QuizSettings s = quiz.getSettings();
        QuizSettingsDto sDto = QuizSettingsDto.builder()
                .durationMinutes(s != null ? s.getDurationMinutes() : 0)
                .passScorePercentage(s != null ? s.getPassScorePercentage() : 50)
                .maxAttempts(s != null ? s.getMaxAttempts() : 1)
                .shuffleQuestions(s != null && Boolean.TRUE.equals(s.getShuffleQuestions()))
                .shuffleAnswers(s != null && Boolean.TRUE.equals(s.getShuffleAnswers()))
                .showResultImmediately(s == null || Boolean.TRUE.equals(s.getShowResultImmediately()))
                .allowReview(s == null || Boolean.TRUE.equals(s.getAllowReview()))
                .startTime(s != null ? s.getStartTime() : null)
                .endTime(s != null ? s.getEndTime() : null)
                .build();

        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .code(quiz.getCode())
                .status(quiz.getStatus())
                .quizType(quiz.getQuizType()) // 👈 Đưa trường này vào response
                .totalQuestions(quiz.getTotalQuestions())
                .totalMarks(quiz.getTotalMarks())
                .creatorName(quiz.getCreator() != null ? quiz.getCreator().getFullName() : "")
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .settings(sDto)
                .build();
    }

    @Override
    @Transactional
    public void deleteQuiz(Long id, String username) {
        Quiz quiz = getQuizAndCheckOwnership(id, username);
        quizRepository.delete(quiz);
    }
}