package com.tuan.exam.entity;

import com.tuan.exam.entity.enums.AttemptStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_attempts",indexes = {
        @Index(name = "idx_attempts_user_quiz", columnList = "user_id, quiz_id"),
        @Index(name = "idx_attempts_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "attempt_number")
    @Builder.Default
    private Integer attemptNumber = 1;

    @Column(name = "start_time", nullable = false, updatable = false)
    private LocalDateTime startTime;

    @Column(name = "submit_time")
    private LocalDateTime submitTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    private Double score;
    // Thêm các trường dữ liệu
    @Column(columnDefinition = "TEXT")
    private String draftAnswers; // Lưu chuỗi JSON chứa các câu trả lời nháp

    @Column(columnDefinition = "integer default 0")
    private Integer violationCount = 0; // Đếm số lần rời khỏi màn hình thi

    @Column(columnDefinition = "boolean default false")
    private Boolean isForceSubmitted = false; // Đánh dấu là bị hệ thống ép nộp (do hết giờ hoặc gian lận)

    @Column(name = "is_passed")
    private Boolean isPassed;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserAnswer> userAnswers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.startTime = LocalDateTime.now();
    }
}
