package com.tuan.exam.repository;

import com.tuan.exam.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    boolean existsByCode(String code);
    // Tìm kiếm môn học theo mã (Ví dụ: tìm môn có code là "MATH")
    Optional<Subject> findByCode(String code);

}
