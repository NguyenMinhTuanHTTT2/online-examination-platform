package com.tuan.exam.service.impl;

import com.tuan.exam.dto.request.QuestionRequest;
import com.tuan.exam.dto.response.AnswerResponse;
import com.tuan.exam.dto.response.QuestionResponse;
import com.tuan.exam.entity.Option;
import com.tuan.exam.entity.Question;
import com.tuan.exam.entity.Quiz;
import com.tuan.exam.entity.Subject;
import com.tuan.exam.repository.QuestionRepository;
import com.tuan.exam.repository.QuizRepository;
import com.tuan.exam.repository.SubjectRepository;
import com.tuan.exam.service.interfaces.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final SubjectRepository subjectRepository;

    @Override
    @Transactional
    public QuestionResponse addQuestionToQuiz(Long quizId, QuestionRequest request, String username) {
        Quiz quiz = getQuizAndCheckOwnership(quizId, username);

        if (request.getDifficulty() == null) {
            throw new RuntimeException("Lỗi: Thuộc tính difficulty truyền vào bị NULL hoặc sai định dạng Enum!");
        }

        if (request.getSubjectId() == null) {
            throw new RuntimeException("Lỗi: Thuộc tính subjectId truyền vào bị NULL!");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học với ID: " + request.getSubjectId()));

        Question question = Question.builder()
                .quiz(quiz)
                .subject(subject)
                .content(request.getContent())
                .questionType(request.getQuestionType())
                .difficulty(request.getDifficulty())
                .scoreWeight((int) 0.0) // Tạm thời để 0, sẽ được chuẩn hóa lại ngay sau đó
                .explanation(request.getExplanation())
                .options(new ArrayList<>())
                .build();

        // Lưu các lựa chọn A, B, C, D vào Option
        if (request.getAnswers() != null) {
            List<Option> options = request.getAnswers().stream().map(a -> Option.builder()
                    .question(question)
                    .content(a.getContent())
                    .isCorrect(a.getIsCorrect())
                    .build()).collect(Collectors.toList());
            question.getOptions().addAll(options);
        }

        // Lưu câu hỏi mới trước để nó xuất hiện trong cơ sở dữ liệu
        Question savedQuestion = questionRepository.save(question);

        // TỰ ĐỘNG CHUẨN HÓA LẠI ĐIỂM SỐ TRÊN THANG 10 CHO TOÀN BỘ ĐỀ THI
        normalizeQuizScores(quiz);

        return mapToResponse(savedQuestion);
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(Long quizId, Long questionId, QuestionRequest request, String username) {
        Quiz quiz = getQuizAndCheckOwnership(quizId, username);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        if (!question.getQuiz().getId().equals(quizId)) {
            throw new RuntimeException("Câu hỏi này không thuộc đề thi đã chọn");
        }

        question.setContent(request.getContent());
        question.setQuestionType(request.getQuestionType());
        question.setDifficulty(request.getDifficulty());
        question.setExplanation(request.getExplanation());

        if (question.getOptions() == null) {
            question.setOptions(new ArrayList<>());
        } else {
            question.getOptions().clear();
        }

        if (request.getAnswers() != null) {
            List<Option> newOptions = request.getAnswers().stream().map(a -> Option.builder()
                    .question(question)
                    .content(a.getContent())
                    .isCorrect(a.getIsCorrect())
                    .build()).collect(Collectors.toList());
            question.getOptions().addAll(newOptions);
        }

        Question updatedQuestion = questionRepository.save(question);

        // Chuẩn hóa lại điểm số (đảm bảo tổng vẫn là 10 sau khi cập nhật nếu số lượng câu thay đổi)
        normalizeQuizScores(quiz);

        return mapToResponse(updatedQuestion);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long quizId, Long questionId, String username) {
        Quiz quiz = getQuizAndCheckOwnership(quizId, username);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        if (!question.getQuiz().getId().equals(quizId)) {
            throw new RuntimeException("Câu hỏi này không thuộc đề thi đã chọn");
        }

        questionRepository.delete(question);

        // TỰ ĐỘNG CHUẨN HÓA LẠI ĐIỂM SỐ KHI XÓA CÂU HỎI
        normalizeQuizScores(quiz);
    }

    @Override
    public List<QuestionResponse> getQuestionsByQuizId(Long quizId, String username) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi với ID: " + quizId));

        List<Question> questions = questionRepository.findByQuizId(quizId);
        return questions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Phương thức phụ trợ: Tự động chia đều điểm số trên thang điểm 10 cho tất cả câu hỏi trong đề thi
     */
    private void normalizeQuizScores(Quiz quiz) {
        List<Question> questions = questionRepository.findByQuizId(quiz.getId());
        int totalQuestions = questions.size();

        quiz.setTotalQuestions(totalQuestions);

        if (totalQuestions == 0) {
            quiz.setTotalMarks((int) 0.0);
        } else {
            // Chia đều 10 điểm cho tổng số câu, làm tròn 2 chữ số thập phân
            double weightPerQuestion = Math.round((10.0 / totalQuestions) * 100.0) / 100.0;

            for (Question q : questions) {
                q.setScoreWeight((int) weightPerQuestion);
            }
            questionRepository.saveAll(questions);
            quiz.setTotalMarks((int) 10.0); // Tổng điểm của đề luôn cố định là 10
        }
        quizRepository.save(quiz);
    }

    private Quiz getQuizAndCheckOwnership(Long quizId, String username) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi"));

        if (!quiz.getCreator().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền thao tác trên đề thi của người khác");
        }
        return quiz;
    }

    private QuestionResponse mapToResponse(Question question) {
        List<AnswerResponse> answerResponses = (question.getOptions() != null ? question.getOptions() : new ArrayList<Option>())
                .stream()
                .map(o -> AnswerResponse.builder()
                        .id(o.getId())
                        .content(o.getContent())
                        .isCorrect(o.getIsCorrect())
                        .build())
                .collect(Collectors.toList());

        return QuestionResponse.builder()
                .id(question.getId())
                .subjectId(question.getSubject() != null ? question.getSubject().getId() : null)
                .content(question.getContent())
                .type(question.getQuestionType())
                .difficulty(question.getDifficulty())
                .scoreWeight(question.getScoreWeight())
                .explanation(question.getExplanation())
                .answers(answerResponses)
                .build();
    }
}