package com.tuan.exam.repository;

import com.tuan.exam.entity.Quiz;
import com.tuan.exam.entity.enums.QuizStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    // Học viên nhập mã để vào phòng thi
    Optional<Quiz> findByCode(String code);

    // Lấy danh sách đề thi do một giảng viên tạo (có phân trang)
    Page<Quiz> findByCreatorId(Long creatorId, Pageable pageable);

    // Lấy danh sách đề thi đang public trên hệ thống
    Page<Quiz> findByStatus(QuizStatus status, Pageable pageable);
}
