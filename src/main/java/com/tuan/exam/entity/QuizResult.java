package com.tuan.exam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_results",indexes = {
        @Index(name = "idx_results_user", columnList = "user_id"),
        @Index(name = "idx_results_quiz", columnList = "quiz_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false, unique = true)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "total_score", nullable = false)
    private Double totalScore;

    @Column(name = "max_possible_score", nullable = false)
    private Double maxPossibleScore;

    @Column(name = "correct_answers_count")
    @Builder.Default
    private Integer correctAnswersCount = 0;

    @Column(name = "wrong_answers_count")
    @Builder.Default
    private Integer wrongAnswersCount = 0;

    @Column(name = "skipped_count")
    @Builder.Default
    private Integer skippedCount = 0;

    @Column(name = "graded_at", nullable = false, updatable = false)
    private LocalDateTime gradedAt;

    @PrePersist
    protected void onCreate() {
        this.gradedAt = LocalDateTime.now();
    }
}
