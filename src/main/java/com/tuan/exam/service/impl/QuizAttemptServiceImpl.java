package com.tuan.exam.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuan.exam.dto.request.QuestionSubmissionDto;
import com.tuan.exam.dto.request.SubmitQuizRequest;
import com.tuan.exam.dto.response.QuestionResultDto;
import com.tuan.exam.dto.response.QuizResultResponse;
import com.tuan.exam.entity.*;
import com.tuan.exam.entity.enums.AttemptStatus;
import com.tuan.exam.entity.enums.QuizStatus;
import com.tuan.exam.repository.*;
import com.tuan.exam.service.interfaces.QuizAttemptService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class QuizAttemptServiceImpl implements QuizAttemptService {

    private final QuizAttemptRepository attemptRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final UserAnswerRepository userAnswerRepository;
    // 1. Khai báo thêm ObjectMapper ở đầu file QuizAttemptServiceImpl để parse JSON
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Long startQuizAttempt(Long quizId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Đề thi không tồn tại"));

        if (quiz.getStatus() != QuizStatus.PUBLISHED) {
            throw new RuntimeException("Đề thi chưa được xuất bản!");
        }

        // Kiểm tra số lần làm bài cho phép (maxAttempts)
        long completedAttempts = attemptRepository.countByUserIdAndQuizId(user.getId(), quizId);

        if (quiz.getSettings().getMaxAttempts() > 0 && completedAttempts >= quiz.getSettings().getMaxAttempts()) {
            throw new RuntimeException("Bạn đã vượt quá số lần làm bài cho phép!");
        }

        // Tạo mới Lượt thi (QuizAttempt) - Dùng đúng tên trường startTime, submitTime, score
        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .user(user)
                .startTime(LocalDateTime.now())
                .status(AttemptStatus.IN_PROGRESS)
                .score(0.0)
                .build();

        return attemptRepository.save(attempt).getId();
    }

    @Override
    @Transactional
    public QuizResultResponse submitQuizAttempt(Long attemptId, SubmitQuizRequest request, String username) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Lượt thi không tồn tại"));

        // BẮT ĐẦU: XỬ LÝ TIMEOUT
        Integer durationMinutes = attempt.getQuiz().getSettings().getDurationMinutes();
        if (durationMinutes != null && durationMinutes > 0) {
            // Cho phép độ trễ mạng (buffer) là 30 giây
            LocalDateTime deadline = attempt.getStartTime().plusMinutes(durationMinutes).plusSeconds(30);
            LocalDateTime now = LocalDateTime.now();

            if (now.isAfter(deadline)) {
                // Học viên nộp quá trễ -> Ép nộp bằng bản nháp cuối cùng trong DB thay vì dữ liệu họ vừa gửi
                attempt.setIsForceSubmitted(true);
                return forceSubmitDraft(attempt);
            }
        }

        if (!attempt.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền nộp bài cho lượt thi này");
        }

        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            throw new RuntimeException("Lượt thi này đã được nộp trước đó!");
        }

        Quiz quiz = attempt.getQuiz();
        LocalDateTime now = LocalDateTime.now();

        // 1. Map dữ liệu nộp bài từ Request để tra cứu nhanh
        Map<Long, QuestionSubmissionDto> submissionMap = new HashMap<>();
        if (request.getAnswers() != null) {
            for (QuestionSubmissionDto sub : request.getAnswers()) {
                submissionMap.put(sub.getQuestionId(), sub);
            }
        }

        // 2. Lấy toàn bộ câu hỏi và đáp án trong DB
        List<Question> questions = questionRepository.findByQuizId(quiz.getId());

        double totalScoreObtained = 0.0;
        int correctQuestionsCount = 0;
        List<QuestionResultDto> details = new ArrayList<>();
        List<UserAnswer> userAnswersToSave = new ArrayList<>();

        // 3. THUẬT TOÁN AUTO SCORING ENGINE
        for (Question question : questions) {
            QuestionSubmissionDto studentSubmission = submissionMap.get(question.getId());

            // Lấy danh sách ID đáp án ĐÚNG từ DB
            Set<Long> correctAnswerIds = new HashSet<>();
            for (Answer answer : question.getAnswers()) {
                if (Boolean.TRUE.equals(answer.getIsCorrect())) {
                    correctAnswerIds.add(answer.getId());
                }
            }

            // Lấy danh sách ID đáp án Học viên chọn
            Set<Long> studentAnswerIds = new HashSet<>();
            Answer selectedAnswerEntity = null;

            if (studentSubmission != null && studentSubmission.getSelectedAnswerIds() != null) {
                studentAnswerIds.addAll(studentSubmission.getSelectedAnswerIds());

                // Tìm Answer Entity tương ứng với ID học viên chọn
                if (!studentSubmission.getSelectedAnswerIds().isEmpty()) {
                    Long selectedId = studentSubmission.getSelectedAnswerIds().get(0);
                    selectedAnswerEntity = question.getAnswers().stream()
                            .filter(a -> a.getId().equals(selectedId))
                            .findFirst()
                            .orElse(null);
                }
            }

            // So sánh 2 Set đáp án: Bằng nhau hoàn toàn -> ĐÚNG
            boolean isCorrect = !correctAnswerIds.isEmpty() && correctAnswerIds.equals(studentAnswerIds);
            double scoreEarned = isCorrect ? question.getScoreWeight().doubleValue() : 0.0;

            if (isCorrect) {
                totalScoreObtained += scoreEarned;
                correctQuestionsCount++;
            }

            // Lưu lịch sử câu trả lời vào bảng UserAnswer (Khớp đúng tên trường selectedAnswer, essayAnswer, marksEarned)
            UserAnswer userAnswer = UserAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .selectedAnswer(selectedAnswerEntity)
                    .essayAnswer(studentSubmission != null ? studentSubmission.getTextAnswer() : null)
                    .isCorrect(isCorrect)
                    .marksEarned(scoreEarned)
                    .build();
            userAnswersToSave.add(userAnswer);

            // Chi tiết kết quả từng câu
            details.add(QuestionResultDto.builder()
                    .questionId(question.getId())
                    .questionContent(question.getContent())
                    .isCorrect(isCorrect)
                    .scoreEarned((int) scoreEarned)
                    .maxScore(question.getScoreWeight())
                    .studentAnswerIds(new ArrayList<>(studentAnswerIds))
                    .correctAnswerIds(quiz.getSettings().getAllowReview() ? new ArrayList<>(correctAnswerIds) : null)
                    .explanation(quiz.getSettings().getAllowReview() ? question.getExplanation() : null)
                    .build());
        }

        // 4. Lưu danh sách câu trả lời
        userAnswerRepository.saveAll(userAnswersToSave);

        // 5. Cập nhật trạng thái và kết quả lượt thi (Khớp với submitTime, score, AttemptStatus.SUBMITTED)
        int totalMarks = quiz.getTotalMarks() > 0 ? quiz.getTotalMarks() : 1;
        double percentage = (totalScoreObtained / totalMarks) * 100;
        boolean isPassed = percentage >= quiz.getSettings().getPassScorePercentage();

        attempt.setSubmitTime(now);
        attempt.setScore(totalScoreObtained);
        attempt.setIsPassed(isPassed);
        attempt.setStatus(AttemptStatus.SUBMITTED);

        attemptRepository.save(attempt);

        // 6. Trả về Response
        return QuizResultResponse.builder()
                .attemptId(attempt.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .scoreObtained((int) totalScoreObtained)
                .totalMarks(totalMarks)
                .percentage(Math.round(percentage * 100.0) / 100.0)
                .isPassed(isPassed)
                .totalQuestions(questions.size())
                .correctCount(correctQuestionsCount)
                .startedAt(attempt.getStartTime())
                .completedAt(now)
                .durationSeconds(Duration.between(attempt.getStartTime(), now).getSeconds())
                .details(quiz.getSettings().getAllowReview() ? details : null)
                .build();
    }

    @Override
    public QuizResultResponse getAttemptResult(Long attemptId, String username) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Lượt thi không tồn tại"));

        if (!attempt.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền xem kết quả này");
        }

        if (attempt.getStatus() != AttemptStatus.SUBMITTED) {
            throw new RuntimeException("Bài thi chưa hoàn thành!");
        }


        return null;
    }
    // 2. Hàm Auto-save (Lưu nháp)
    @Override
    @Transactional
    public void autoSave(Long attemptId, SubmitQuizRequest request, String username) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Lượt thi không tồn tại"));

        if (!attempt.getUser().getUsername().equals(username) || attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            return; // Bỏ qua nếu không hợp lệ, không cần ném lỗi để tránh crash Frontend
        }

        try {
            // Chuyển Request thành chuỗi JSON và lưu vào DB
            String draftJson = objectMapper.writeValueAsString(request.getAnswers());
            attempt.setDraftAnswers(draftJson);
            attemptRepository.save(attempt);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    // Hàm phụ trợ ép nộp bài bằng bản nháp (Dùng chung cho cả Timeout và Vi phạm)
    @Override
    @Transactional
    public QuizResultResponse forceSubmitDraft(QuizAttempt attempt) {
        // 1. Lấy chuỗi JSON bản nháp trong DB ra để parse lại thành Request
        SubmitQuizRequest draftRequest = new SubmitQuizRequest();
        if (attempt.getDraftAnswers() != null && !attempt.getDraftAnswers().isEmpty()) {
            try {
                List<QuestionSubmissionDto> answers = objectMapper.readValue(
                        attempt.getDraftAnswers(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionSubmissionDto.class)
                );
                draftRequest.setAnswers(answers);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else {
            // Nếu chưa từng lưu nháp câu nào thì truyền list rỗng
            draftRequest.setAnswers(new ArrayList<>());
        }

        // 2. Tái sử dụng lại luôn logic chấm điểm chuẩn của hàm submitQuizAttempt có sẵn
        return submitQuizAttempt(attempt.getId(), draftRequest, attempt.getUser().getUsername());
    }

    // 3. Hàm Ghi nhận Vi phạm (Anti-cheat)
    @Override
    @Transactional
    public QuizResultResponse reportViolation(Long attemptId, String username) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Lượt thi không tồn tại"));

        if (!attempt.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Không hợp lệ");
        }

        // Tăng số lần vi phạm
        attempt.setViolationCount(attempt.getViolationCount() + 1);

        // Giả sử luật: Quá 3 lần chuyển tab -> Tự động nộp bài luôn
        int MAX_VIOLATIONS = 3;
        if (attempt.getViolationCount() >= MAX_VIOLATIONS) {
            attempt.setIsForceSubmitted(true);
            attemptRepository.save(attempt);

            // Cần parse lại draftAnswers để nộp (Sẽ dùng hàm chấm điểm nội bộ)
            return forceSubmitDraft(attempt);
        }

        attemptRepository.save(attempt);
        return null; // Chưa bị ép nộp
    }


}
