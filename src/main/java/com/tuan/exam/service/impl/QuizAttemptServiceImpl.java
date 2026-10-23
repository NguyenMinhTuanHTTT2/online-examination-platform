package com.tuan.exam.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuan.exam.dto.request.QuestionSubmissionDto;
import com.tuan.exam.dto.request.SubmitQuizRequest;
import com.tuan.exam.dto.response.OptionDto;
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
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizAttemptServiceImpl implements QuizAttemptService {

    private final QuizAttemptRepository attemptRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final UserAnswerRepository userAnswerRepository;
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

        long completedAttempts = attemptRepository.countByUserIdAndQuizId(user.getId(), quizId);

        if (quiz.getSettings() != null && quiz.getSettings().getMaxAttempts() > 0 && completedAttempts >= quiz.getSettings().getMaxAttempts()) {
            throw new RuntimeException("Bạn đã vượt quá số lần làm bài cho phép!");
        }

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .user(user)
                .startTime(Instant.now())
                .status(AttemptStatus.IN_PROGRESS)
                .score(0.0)
                .violationCount(0)
                .build();

        return attemptRepository.save(attempt).getId();
    }

    @Override
    @Transactional
    public Map<String, Object> getAttemptForTake(Long attemptId, String username) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Lượt thi không tồn tại"));

        if (!attempt.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền truy cập lượt thi này");
        }

        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            throw new RuntimeException("Lượt thi này đã nộp bài trước đó!");
        }

        Quiz quiz = attempt.getQuiz();
        Instant now = Instant.now();

        Integer durationMinutes = quiz.getSettings() != null ? quiz.getSettings().getDurationMinutes() : 0;
        long remainingSeconds = 0;
        if (durationMinutes != null && durationMinutes > 0) {
            Instant endTime = attempt.getStartTime().plus(Duration.ofMinutes(durationMinutes));
            remainingSeconds = Duration.between(now, endTime).getSeconds();
            if (remainingSeconds < 0) {
                remainingSeconds = 0;
            }
        }

        List<Question> questions = questionRepository.findByQuizId(quiz.getId());
        if ((questions == null || questions.isEmpty()) && quiz.getQuestions() != null) {
            questions = new ArrayList<>(quiz.getQuestions());
        }

        List<Map<String, Object>> questionList = new ArrayList<>();

        for (Question q : questions) {
            Map<String, Object> qMap = new HashMap<>();
            qMap.put("id", q.getId());
            qMap.put("questionId", q.getId());
            qMap.put("content", q.getContent());
            qMap.put("points", q.getScoreWeight() != null ? q.getScoreWeight() : 1);
            qMap.put("imageUrl", q.getImageUrl());

            List<Map<String, Object>> optionsList = new ArrayList<>();

            if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                for (Option optEntity : q.getOptions()) {
                    Map<String, Object> opt = new HashMap<>();
                    opt.put("id", optEntity.getId());
                    opt.put("content", optEntity.getContent());
                    optionsList.add(opt);
                }
            }

            qMap.put("options", optionsList);
            questionList.add(qMap);
        }

        Object draftAnswers = null;
        if (attempt.getDraftAnswers() != null && !attempt.getDraftAnswers().isEmpty()) {
            try {
                draftAnswers = objectMapper.readValue(attempt.getDraftAnswers(), Object.class);
            } catch (Exception e) {
                draftAnswers = new HashMap<>();
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("attemptId", attempt.getId());
        response.put("quizTitle", quiz.getTitle());
        response.put("startTime", attempt.getStartTime());
        response.put("remainingSeconds", remainingSeconds);
        response.put("questions", questionList);
        response.put("draftAnswers", draftAnswers);

        Map<String, Object> attemptObj = new HashMap<>();
        attemptObj.put("id", attempt.getId());
        attemptObj.put("startTime", attempt.getStartTime());
        attemptObj.put("status", attempt.getStatus());
        response.put("attempt", attemptObj);

        return response;
    }

    @Override
    @Transactional
    public QuizResultResponse submitQuizAttempt(Long attemptId, SubmitQuizRequest request, String username) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Lượt thi không tồn tại"));

        if (!attempt.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền nộp bài cho lượt thi này");
        }

        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            return getAttemptResult(attemptId, username);
        }

        Integer durationMinutes = attempt.getQuiz().getSettings() != null ? attempt.getQuiz().getSettings().getDurationMinutes() : 0;
        if (durationMinutes != null && durationMinutes > 0) {
            Instant deadline = attempt.getStartTime()
                    .plus(Duration.ofMinutes(durationMinutes))
                    .plusSeconds(30);
            if (Instant.now().isAfter(deadline)) {
                attempt.setIsForceSubmitted(true);
                return forceSubmitDraft(attempt);
            }
        }

        List<QuestionSubmissionDto> answers = (request != null && request.getAnswers() != null) ? request.getAnswers() : new ArrayList<>();

        try {
            if (!answers.isEmpty()) {
                String jsonAnswers = objectMapper.writeValueAsString(answers);
                attempt.setDraftAnswers(jsonAnswers);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return processScoringAndSave(attempt, answers);
    }

    @Override
    @Transactional
    public QuizResultResponse forceSubmitDraft(QuizAttempt attempt) {
        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            return getAttemptResult(attempt.getId(), attempt.getUser().getUsername());
        }

        List<QuestionSubmissionDto> answers = parseDraftAnswers(attempt.getDraftAnswers());
        attempt.setIsForceSubmitted(true);
        return processScoringAndSave(attempt, answers);
    }

    private List<QuestionSubmissionDto> parseDraftAnswers(String draftJson) {
        if (draftJson == null || draftJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(
                    draftJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionSubmissionDto.class)
            );
        } catch (Exception e1) {
            try {
                Map<String, Object> map = objectMapper.readValue(draftJson, new TypeReference<Map<String, Object>>() {});
                List<QuestionSubmissionDto> list = new ArrayList<>();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Long qId = Long.parseLong(entry.getKey());
                    List<Long> selected = new ArrayList<>();

                    if (entry.getValue() instanceof List) {
                        for (Object item : (List<?>) entry.getValue()) {
                            selected.add(Long.parseLong(item.toString()));
                        }
                    } else if (entry.getValue() != null) {
                        selected.add(Long.parseLong(entry.getValue().toString()));
                    }

                    QuestionSubmissionDto dto = new QuestionSubmissionDto();
                    dto.setQuestionId(qId);
                    dto.setSelectedAnswerIds(selected);
                    list.add(dto);
                }
                return list;
            } catch (Exception e2) {
                return new ArrayList<>();
            }
        }
    }

    private QuizResultResponse processScoringAndSave(QuizAttempt attempt, List<QuestionSubmissionDto> submissions) {
        Quiz quiz = attempt.getQuiz();
        Instant now = Instant.now();

        Map<Long, QuestionSubmissionDto> submissionMap = new HashMap<>();
        if (submissions != null) {
            for (QuestionSubmissionDto sub : submissions) {
                if (sub != null && sub.getQuestionId() != null) {
                    submissionMap.put(sub.getQuestionId(), sub);
                }
            }
        }

        List<Question> questions = questionRepository.findByQuizId(quiz.getId());
        if ((questions == null || questions.isEmpty()) && quiz.getQuestions() != null) {
            questions = new ArrayList<>(quiz.getQuestions());
        }

        int totalQuestions = questions.size();
        double scorePerQuestion = totalQuestions > 0 ? (10.0 / totalQuestions) : 0.0;

        int correctCount = 0;
        int incorrectCount = 0;
        int unansweredCount = 0;

        List<QuestionResultDto> details = new ArrayList<>();
        List<UserAnswer> userAnswersToSave = new ArrayList<>();

        for (Question question : questions) {
            QuestionSubmissionDto studentSubmission = submissionMap.get(question.getId());

            Set<Long> correctAnswerIds = new HashSet<>();
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                correctAnswerIds = question.getOptions().stream()
                        .filter(o -> Boolean.TRUE.equals(o.getIsCorrect()))
                        .map(Option::getId)
                        .collect(Collectors.toSet());
            }

            Set<Long> studentAnswerIds = new HashSet<>();
            if (studentSubmission != null && studentSubmission.getSelectedAnswerIds() != null) {
                studentAnswerIds.addAll(studentSubmission.getSelectedAnswerIds());
            }

            String textAns = studentSubmission != null ? studentSubmission.getTextAnswer() : null;
            boolean isUnanswered = studentAnswerIds.isEmpty() && (textAns == null || textAns.isBlank());

            boolean isCorrect = !isUnanswered && !correctAnswerIds.isEmpty() && correctAnswerIds.equals(studentAnswerIds);

            double scoreEarned = isCorrect ? scorePerQuestion : 0.0;

            if (isCorrect) {
                correctCount++;
            } else if (isUnanswered) {
                unansweredCount++;
            } else {
                incorrectCount++;
            }

            Option selectedOptionEntity = null;
            if (!studentAnswerIds.isEmpty() && question.getOptions() != null && !question.getOptions().isEmpty()) {
                Long selectedId = studentAnswerIds.iterator().next();
                selectedOptionEntity = question.getOptions().stream()
                        .filter(o -> o.getId().equals(selectedId))
                        .findFirst()
                        .orElse(null);
            }

            UserAnswer userAnswer = UserAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .selectedOption(selectedOptionEntity)
                    .essayAnswer(textAns)
                    .isCorrect(isCorrect)
                    .marksEarned(scoreEarned)
                    .build();
            userAnswersToSave.add(userAnswer);

            List<OptionDto> optionDtos = new ArrayList<>();
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                optionDtos = question.getOptions().stream()
                        .map(o -> OptionDto.builder()
                                .id(o.getId())
                                .content(o.getContent())
                                .build())
                        .collect(Collectors.toList());
            }

            boolean allowReview = quiz.getSettings() == null || Boolean.TRUE.equals(quiz.getSettings().getAllowReview());

            details.add(QuestionResultDto.builder()
                    .questionId(question.getId())
                    .questionContent(question.getContent())
                    .imageUrl(question.getImageUrl())
                    .isCorrect(isCorrect)
                    .scoreEarned(scoreEarned)
                    .maxScore(scorePerQuestion)
                    .studentAnswerIds(new ArrayList<>(studentAnswerIds))
                    .correctAnswerIds(allowReview ? new ArrayList<>(correctAnswerIds) : null)
                    .options(optionDtos)
                    .explanation(allowReview ? question.getExplanation() : null)
                    .build());
        }

        userAnswerRepository.saveAll(userAnswersToSave);

        // 🎯 TÍNH ĐIỂM TỔNG KẾT CHUẨN XÁC THEO TỶ LỆ SỐ CÂU ĐÚNG / TỔNG SỐ CÂU (Thang 10 điểm)
        // Ví dụ: Đúng 5/5 câu -> (5/5) * 10 = 10.0 điểm
        double totalScoreObtained = totalQuestions > 0
                ? Math.round(((double) correctCount / totalQuestions) * 10.0 * 10.0) / 10.0
                : 0.0;

        double percentage = totalQuestions > 0 ? ((double) correctCount / totalQuestions) * 100.0 : 0.0;
        int passScore = (quiz.getSettings() != null && quiz.getSettings().getPassScorePercentage() != null)
                ? quiz.getSettings().getPassScorePercentage() : 50;
        boolean isPassed = percentage >= passScore;

        long durationSec = Duration.between(attempt.getStartTime(), now).getSeconds();

        attempt.setSubmitTime(now);
        attempt.setScore(totalScoreObtained);
        attempt.setIsPassed(isPassed);
        attempt.setStatus(AttemptStatus.SUBMITTED);

        attemptRepository.save(attempt);

        boolean allowReview = quiz.getSettings() == null || Boolean.TRUE.equals(quiz.getSettings().getAllowReview());

        return QuizResultResponse.builder()
                .attemptId(attempt.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .scoreObtained(totalScoreObtained) // Trả về Double chuẩn xác (ví dụ 10.0)
                .totalMarks(10) // Thang điểm tối đa 10
                .percentage(Math.round(percentage * 100.0) / 100.0)
                .isPassed(isPassed)
                .totalQuestions(totalQuestions)
                .correctCount(correctCount)
                .incorrectCount(incorrectCount)
                .unansweredCount(unansweredCount)
                .startedAt(attempt.getStartTime())
                .completedAt(now)
                .durationSeconds(durationSec)
                .details(allowReview ? details : null)
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

        Quiz quiz = attempt.getQuiz();
        List<UserAnswer> userAnswers = userAnswerRepository.findByAttemptId(attemptId);
        List<Question> questions = questionRepository.findByQuizId(quiz.getId());
        if ((questions == null || questions.isEmpty()) && quiz.getQuestions() != null) {
            questions = new ArrayList<>(quiz.getQuestions());
        }

        List<QuestionSubmissionDto> draftSubmissions = parseDraftAnswers(attempt.getDraftAnswers());
        Map<Long, QuestionSubmissionDto> submissionMap = new HashMap<>();
        if (draftSubmissions != null) {
            for (QuestionSubmissionDto sub : draftSubmissions) {
                if (sub != null && sub.getQuestionId() != null) {
                    submissionMap.put(sub.getQuestionId(), sub);
                }
            }
        }

        int totalQuestions = questions.size();
        double scorePerQuestion = totalQuestions > 0 ? (10.0 / totalQuestions) : 0.0;

        int correctCount = 0;
        int incorrectCount = 0;
        int unansweredCount = 0;

        List<QuestionResultDto> details = new ArrayList<>();
        boolean allowReview = quiz.getSettings() == null || Boolean.TRUE.equals(quiz.getSettings().getAllowReview());

        for (Question q : questions) {
            UserAnswer ua = userAnswers.stream()
                    .filter(u -> u.getQuestion().getId().equals(q.getId()))
                    .findFirst()
                    .orElse(null);

            QuestionSubmissionDto studentSubmission = submissionMap.get(q.getId());

            Set<Long> correctAnswerIds = new HashSet<>();
            if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                correctAnswerIds = q.getOptions().stream()
                        .filter(o -> Boolean.TRUE.equals(o.getIsCorrect()))
                        .map(Option::getId)
                        .collect(Collectors.toSet());
            }

            List<Long> studentAnswerIds = new ArrayList<>();
            if (studentSubmission != null && studentSubmission.getSelectedAnswerIds() != null) {
                studentAnswerIds.addAll(studentSubmission.getSelectedAnswerIds());
            } else if (ua != null && ua.getSelectedOption() != null) {
                studentAnswerIds.add(ua.getSelectedOption().getId());
            }

            String textAns = studentSubmission != null ? studentSubmission.getTextAnswer() : (ua != null ? ua.getEssayAnswer() : null);

            boolean isUnanswered = studentAnswerIds.isEmpty() && (textAns == null || textAns.isBlank());

            boolean isCorrect = (ua != null && Boolean.TRUE.equals(ua.getIsCorrect())) ||
                    (!isUnanswered && !correctAnswerIds.isEmpty() && correctAnswerIds.equals(new HashSet<>(studentAnswerIds)));

            if (isCorrect) {
                correctCount++;
            } else if (isUnanswered) {
                unansweredCount++;
            } else {
                incorrectCount++;
            }

            List<OptionDto> optionDtos = new ArrayList<>();
            if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                optionDtos = q.getOptions().stream()
                        .map(o -> OptionDto.builder()
                                .id(o.getId())
                                .content(o.getContent())
                                .build())
                        .collect(Collectors.toList());
            }

            double scoreEarned = (ua != null && ua.getMarksEarned() != null) ? ua.getMarksEarned() : (isCorrect ? scorePerQuestion : 0.0);

            details.add(QuestionResultDto.builder()
                    .questionId(q.getId())
                    .questionContent(q.getContent())
                    .imageUrl(q.getImageUrl())
                    .isCorrect(isCorrect)
                    .scoreEarned(scoreEarned)
                    .maxScore(scorePerQuestion)
                    .studentAnswerIds(studentAnswerIds)
                    .correctAnswerIds(allowReview ? new ArrayList<>(correctAnswerIds) : null)
                    .options(optionDtos)
                    .explanation(allowReview ? q.getExplanation() : null)
                    .build());
        }

        // Lấy điểm đã lưu từ attempt (hoặc tính lại theo số câu đúng)
        double attemptScore = attempt.getScore() != null ? attempt.getScore() :
                (totalQuestions > 0 ? Math.round(((double) correctCount / totalQuestions) * 10.0 * 10.0) / 10.0 : 0.0);

        double percentage = totalQuestions > 0 ? ((double) correctCount / totalQuestions) * 100.0 : 0.0;

        long durationSec = (attempt.getSubmitTime() != null && attempt.getStartTime() != null)
                ? Duration.between(attempt.getStartTime(), attempt.getSubmitTime()).getSeconds() : 0;

        return QuizResultResponse.builder()
                .attemptId(attempt.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .scoreObtained(attemptScore) // Lấy đúng điểm tổng kết của lượt thi
                .totalMarks(10)
                .percentage(Math.round(percentage * 100.0) / 100.0)
                .isPassed(Boolean.TRUE.equals(attempt.getIsPassed()))
                .totalQuestions(totalQuestions)
                .correctCount(correctCount)
                .incorrectCount(incorrectCount)
                .unansweredCount(unansweredCount)
                .startedAt(attempt.getStartTime())
                .completedAt(attempt.getSubmitTime())
                .durationSeconds(durationSec)
                .details(allowReview ? details : null)
                .build();
    }

    @Override
    @Transactional
    public void autoSave(Long attemptId, SubmitQuizRequest request, String username) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Lượt thi không tồn tại"));

        if (!attempt.getUser().getUsername().equals(username) || attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            return;
        }

        try {
            if (request != null && request.getAnswers() != null) {
                String draftJson = objectMapper.writeValueAsString(request.getAnswers());
                attempt.setDraftAnswers(draftJson);
                attemptRepository.save(attempt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public QuizResultResponse reportViolation(Long attemptId, String username) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Lượt thi không tồn tại"));

        if (!attempt.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Không hợp lệ");
        }

        int currentViolations = attempt.getViolationCount() != null ? attempt.getViolationCount() : 0;
        attempt.setViolationCount(currentViolations + 1);

        int MAX_VIOLATIONS = 3;
        if (attempt.getViolationCount() >= MAX_VIOLATIONS) {
            attempt.setIsForceSubmitted(true);
            attemptRepository.save(attempt);
            return forceSubmitDraft(attempt);
        }

        attemptRepository.save(attempt);
        return null;
    }
}