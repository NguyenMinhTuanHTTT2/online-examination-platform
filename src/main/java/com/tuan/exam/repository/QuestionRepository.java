package com.tuan.exam.repository;

import com.tuan.exam.entity.Question;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question,Long> {
    @EntityGraph(attributePaths = {"options"})
    List<Question> findByQuizId(Long quizId);

    // ================= BỔ SUNG CHO TRANG THI THỬ & LẤY SỐ LƯỢNG =================
    // Đếm tổng số câu hỏi thuộc một bài thi (Quiz) cụ thể
    long countByQuizId(Long quizId);

    // BỔ SUNG PHƯƠNG THỨC NÀY ĐỂ LẤY CÂU HỎI NGẪU NHÂN THEO ĐỘ KHÓ VÀ MÔN HỌC:
    @Query(value = "SELECT * FROM questions q WHERE q.subject_id = :subjectId AND q.difficulty = :difficulty ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomQuestions(@Param("subjectId") Long subjectId,
                                       @Param("difficulty") String difficulty,
                                       @Param("limit") int limit);

    // ================= BỔ SUNG CHO TRANG TỔNG QUAN =================
    // Đếm tổng số câu hỏi thuộc các đề do Giáo viên tạo (Question -> Quiz -> Creator -> Id)
    long countByQuizCreatorId(Long creatorId);
}