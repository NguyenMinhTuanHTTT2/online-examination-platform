package com.tuan.exam.entity;

import com.tuan.exam.entity.enums.QuizStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes",indexes= {
        @Index(name = "idx_quizzes_creator", columnList = "creator_id"),
        @Index(name = "idx_quizzes_status", columnList = "status"),
        @Index(name = "idx_quizzes_code", columnList = "code")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Quiz extends BaseEntity  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private QuizStatus status = QuizStatus.DRAFT;

    @Column(name = "total_questions")
    @Builder.Default
    private Integer totalQuestions = 0;

    @Column(name = "total_marks")
    @Builder.Default
    private Integer totalMarks = 0;

    @OneToOne(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private QuizSettings settings;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Question> questions = new ArrayList<>();
}
