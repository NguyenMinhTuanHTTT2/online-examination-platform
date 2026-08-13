package com.tuan.exam.repository;

import com.tuan.exam.entity.QuizResult;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    Optional<QuizResult> findByAttemptId(Long attemptId);

    // Lịch sử điểm của 1 học viên
    Page<QuizResult> findByUserId(Long userId, Pageable pageable);

    // Bảng xếp hạng / Danh sách điểm của 1 đề thi cụ thể
    Page<QuizResult> findByQuizId(Long quizId, Pageable pageable);

    // Đọc từng lô 1000 dòng từ Database
    @QueryHints(value = @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE, value = "1000"))
    @Query("SELECT r FROM QuizResult r JOIN FETCH r.user WHERE r.quiz.id = :quizId")
    Stream<QuizResult> streamByQuizId(@Param("quizId") Long quizId);
}
