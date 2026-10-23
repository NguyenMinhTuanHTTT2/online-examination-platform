package com.tuan.exam.repository;

import com.tuan.exam.entity.Quiz;
import com.tuan.exam.entity.enums.QuizStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    // Học viên nhập mã để vào phòng thi
    Optional<Quiz> findByCode(String code);

    // Lấy danh sách đề thi do một giảng viên tạo (có phân trang)
    Page<Quiz> findByCreatorId(Long creatorId, Pageable pageable);

    // Lấy danh sách đề thi đang public trên hệ thống
    Page<Quiz> findByStatus(QuizStatus status, Pageable pageable);

    // ================= BỔ SUNG CHO TRANG TỔNG QUAN =================
    // 1. Đếm tổng số đề thi của giáo viên
    long countByCreatorId(Long creatorId);

    // 2. Đếm số đề thi theo trạng thái (Ví dụ: PUBLISHED)
    long countByCreatorIdAndStatus(Long creatorId, QuizStatus status);

    // 3. Lấy 5 đề thi mới nhất do giáo viên tạo
    List<Quiz> findTop5ByCreatorIdOrderByIdDesc(Long creatorId);
    // Sử dụng câu lệnh JPQL này để nó tự động bỏ qua điều kiện nếu tham số là null
    @Query("SELECT q FROM Quiz q LEFT JOIN q.settings s WHERE " +
            "q.quizType = 'PRACTICE' AND " +
            "q.status = com.tuan.exam.entity.enums.QuizStatus.PUBLISHED AND " +
            "(:subjectId IS NULL OR q.subject.id = :subjectId) AND " +
            "(:difficulty IS NULL OR s.difficulty = :difficulty) AND " +
            "(:search IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Quiz> filterPracticeQuizzes(
            @Param("subjectId") Long subjectId,
            @Param("difficulty") String difficulty,
            @Param("search") String search
    );

    // ================= BỔ SUNG CHO TRANG THI THỬ (MOCK_TEST) =================
    // Lọc danh sách đề thi thử (MOCK_TEST) theo môn học và từ khóa tìm kiếm
    @Query("SELECT q FROM Quiz q LEFT JOIN q.subject sub WHERE " +
            "q.quizType = 'MOCK_TEST' AND " +
            "q.status = com.tuan.exam.entity.enums.QuizStatus.PUBLISHED AND " +
            "(:subjectId IS NULL OR sub.id = :subjectId) AND " +
            "(:search IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Quiz> filterMockTestQuizzes(
            @Param("subjectId") Long subjectId,
            @Param("search") String search
    );
}
