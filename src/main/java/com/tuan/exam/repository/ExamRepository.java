package com.tuan.exam.repository;

import com.tuan.exam.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam,Long> {
    // Tìm các kỳ thi do giáo viên cụ thể tạo
    List<Exam> findByCreatorId(Long creatorId);

    // Tìm theo trạng thái (Đang mở, Nháp, Đã đóng...)
    List<Exam> findByStatus(String status);

    // Tìm theo loại kỳ thi ("Đề luyện tập" hoặc "Đề thi thử")
    List<Exam> findByExamType(String examType);

    long countByCreatorIdAndStatus(Long creatorId, String status);

    List<Exam> findTop5ByCreatorIdOrderByIdDesc(Long creatorId);
}
