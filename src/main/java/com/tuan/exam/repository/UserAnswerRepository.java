package com.tuan.exam.repository;

import com.tuan.exam.entity.UserAnswer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    // Lấy 1 đáp án cụ thể trong 1 lượt thi
    Optional<UserAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    // Kéo toàn bộ bài làm của lượt thi kèm theo thông tin câu hỏi để chấm điểm
    @EntityGraph(attributePaths = {"question", "selectedAnswer"})
    List<UserAnswer> findByAttemptId(Long attemptId);
}
