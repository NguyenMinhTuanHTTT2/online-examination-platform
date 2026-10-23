package com.tuan.exam.repository;

import com.tuan.exam.entity.QuizAttempt;
import com.tuan.exam.entity.enums.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // ================= BỔ SUNG 2 HÀM NÀY CHO TRANG TỔNG QUAN =================

    // 1. Đếm tổng số lượt đã làm xong (COMPLETED) trong toàn bộ MySQL
    long countByStatus(AttemptStatus status);

    // 2. Lấy 5 lượt làm bài mới hoàn thành gần đây nhất để hiện vào mục "Hoạt động gần đây"
    List<QuizAttempt> findTop5ByStatusOrderByIdDesc(AttemptStatus status);

    // ================= BỔ SUNG CHO TRANG TỔNG QUAN =================
    // 1. Đếm tổng lượt thi theo ID Giáo viên (Tự nhảy qua Quiz -> Creator -> Id)
    long countByQuizCreatorIdAndStatus(Long creatorId, AttemptStatus status);

    // 2. Lấy 5 lượt làm bài mới nhất theo ID Giáo viên
    List<QuizAttempt> findTop5ByQuizCreatorIdAndStatusOrderByIdDesc(Long creatorId, AttemptStatus status);

    // 3. Lấy tất cả lượt làm bài theo Giáo viên (dùng để đếm/thống kê nếu cần)
    List<QuizAttempt> findByQuizCreatorIdAndStatus(Long creatorId, AttemptStatus status);

    // 1. Lấy tất cả lượt làm bài của 1 học sinh theo Trạng thái (COMPLETED hoặc IN_PROGRESS)
    List<QuizAttempt> findByUserIdAndStatus(Long userId, AttemptStatus status);

    // 2. Lấy các lượt làm bài đã hoàn thành gần đây nhất của học sinh (xắp xếp giảm dần theo thời gian nộp)
    List<QuizAttempt> findByUserIdAndStatusOrderBySubmitTimeDesc(Long userId, AttemptStatus status);
}
