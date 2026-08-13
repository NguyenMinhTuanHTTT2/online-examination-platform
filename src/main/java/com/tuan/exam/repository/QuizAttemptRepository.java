package com.tuan.exam.repository;

import com.tuan.exam.entity.QuizAttempt;
import com.tuan.exam.entity.enums.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    // Lấy tất cả lịch sử thi của một học viên đối với 1 đề cụ thể
    List<QuizAttempt> findByUserIdAndQuizId(Long userId, Long quizId);

    // Kiểm tra xem user có bài thi nào đang "IN_PROGRESS" không (ngăn chặn thi 2 bài cùng lúc hoặc gian lận)
    Optional<QuizAttempt> findByUserIdAndQuizIdAndStatus(Long userId, Long quizId, AttemptStatus status);

    // Đếm số lần học viên đã thi đề này (để validate với cấu hình max_attempts của Quiz)
    long countByUserIdAndQuizId(Long userId, Long quizId);
    // Bổ sung thêm method này để đếm số lần đã hoàn thành (COMPLETED)
    long countByUserIdAndQuizIdAndStatus(Long userId, Long quizId, AttemptStatus status);
    List<QuizAttempt> findByStatus(AttemptStatus status);
    List<QuizAttempt> findByQuizIdAndStatus(Long quizId, AttemptStatus status);
}
