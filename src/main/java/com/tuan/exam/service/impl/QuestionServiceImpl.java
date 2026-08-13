package com.tuan.exam.service.impl;

import com.tuan.exam.dto.request.QuestionRequest;
import com.tuan.exam.dto.response.AnswerResponse;
import com.tuan.exam.dto.response.QuestionResponse;
import com.tuan.exam.entity.Answer;
import com.tuan.exam.entity.Question;
import com.tuan.exam.entity.Quiz;
import com.tuan.exam.repository.QuestionRepository;
import com.tuan.exam.repository.QuizRepository;
import com.tuan.exam.service.interfaces.QuestionService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    @Override
    @Transactional
    public QuestionResponse addQuestionToQuiz(Long quizId, QuestionRequest request, String username) {
        Quiz quiz = getQuizAndCheckOwnership(quizId, username);

        // 1. Khởi tạo câu hỏi
        Question question = Question.builder()
                .quiz(quiz)
                .content(request.getContent())
                .type(request.getType())
                .scoreWeight(request.getScoreWeight())
                .explanation(request.getExplanation())
                .build();

        // 2. Map đáp án (và liên kết chiều Answer -> Question)
        if (request.getAnswers() != null) {
            List<Answer> answers = request.getAnswers().stream().map(a -> Answer.builder()
                    .question(question) // Bắt buộc set để lưu khóa ngoại
                    .content(a.getContent())
                    .isCorrect(a.getIsCorrect())
                    .build()).collect(Collectors.toList());
            question.getAnswers().addAll(answers);
        }

        // 3. Cập nhật lại tổng số câu và tổng điểm của Quiz
        quiz.setTotalQuestions(quiz.getTotalQuestions() + 1);
        quiz.setTotalMarks(quiz.getTotalMarks() + request.getScoreWeight());
        quizRepository.save(quiz); // JPA tự động lưu Question & Answers vì CascadeType.ALL

        Question savedQuestion = questionRepository.save(question);
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

        // Trừ điểm cũ, cộng điểm mới cho Quiz
        quiz.setTotalMarks(quiz.getTotalMarks() - question.getScoreWeight() + request.getScoreWeight());

        // Cập nhật câu hỏi
        question.setContent(request.getContent());
        question.setType(request.getType());
        question.setScoreWeight(request.getScoreWeight());
        question.setExplanation(request.getExplanation());

        // Xóa đáp án cũ và thêm đáp án mới (orphanRemoval = true sẽ xóa trong DB)
        question.getAnswers().clear();
        if (request.getAnswers() != null) {
            List<Answer> newAnswers = request.getAnswers().stream().map(a -> Answer.builder()
                    .question(question)
                    .content(a.getContent())
                    .isCorrect(a.getIsCorrect())
                    .build()).collect(Collectors.toList());
            question.getAnswers().addAll(newAnswers);
        }

        quizRepository.save(quiz); // Lưu thay đổi của Quiz tổng
        return mapToResponse(questionRepository.save(question));
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

        // Trừ số lượng câu và tổng điểm
        quiz.setTotalQuestions(quiz.getTotalQuestions() - 1);
        quiz.setTotalMarks(quiz.getTotalMarks() - question.getScoreWeight());
        quizRepository.save(quiz);

        questionRepository.delete(question); // orphanRemoval tự xóa luôn các Answer liên quan
    }

    @Override
    public List<QuestionResponse> getQuestionsByQuizId(Long quizId, String username) {
        // Chỉ giáo viên tạo ra đề thi hoặc người thi (xử lý sau) mới được xem danh sách này (có cả đáp án đúng)
        // Hiện tại ta check quyền giáo viên
        getQuizAndCheckOwnership(quizId, username);

        List<Question> questions = questionRepository.findByQuizId(quizId);
        return questions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // --- Helper Methods ---

    private Quiz getQuizAndCheckOwnership(Long quizId, String username) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi"));

        if (!quiz.getCreator().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền thao tác trên đề thi của người khác");
        }
        // Có thể thêm rule: Không cho sửa/xóa câu hỏi nếu đề đã PUBLISHED hoặc có người đang thi
        return quiz;
    }

    private QuestionResponse mapToResponse(Question question) {
        List<AnswerResponse> answerResponses = question.getAnswers().stream()
                .map(a -> AnswerResponse.builder()
                        .id(a.getId())
                        .content(a.getContent())
                        .isCorrect(a.getIsCorrect())
                        .build())
                .collect(Collectors.toList());

        return QuestionResponse.builder()
                .id(question.getId())
                .content(question.getContent())
                .type(question.getType())
                .scoreWeight(question.getScoreWeight())
                .explanation(question.getExplanation())
                .answers(answerResponses)
                .build();
    }

}
