package com.tuan.exam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSettings {
    @Id
    private Long quizId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "pass_score_percentage")
    @Builder.Default
    private Integer passScorePercentage = 50;

    @Column(name = "max_attempts")
    @Builder.Default
    private Integer maxAttempts = 1;

    @Column(name = "shuffle_questions")
    @Builder.Default
    private Boolean shuffleQuestions = false;

    @Column(name = "shuffle_answers")
    @Builder.Default
    private Boolean shuffleAnswers = false;

    @Column(name = "show_result_immediately")
    @Builder.Default
    private Boolean showResultImmediately = true;

    @Column(name = "allow_review")
    @Builder.Default
    private Boolean allowReview = true;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;
}
