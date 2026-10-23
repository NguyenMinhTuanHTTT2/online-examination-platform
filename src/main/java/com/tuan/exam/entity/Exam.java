package com.tuan.exam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "exams", indexes = {
        @Index(name = "idx_exams_quiz", columnList = "quiz_id"),
        @Index(name = "idx_exams_creator", columnList = "creator_id"),
        @Index(name = "idx_exams_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "exam_type", nullable = false, length = 50)
    @Builder.Default
    private String examType = "Đề luyện tập"; // Phân loại: "Đề luyện tập" hoặc "Đề thi thử"

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(length = 20)
    @Builder.Default
    private String status = "DRAFT"; // Trạng thái: DRAFT, UPCOMING, OPEN, CLOSED

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
