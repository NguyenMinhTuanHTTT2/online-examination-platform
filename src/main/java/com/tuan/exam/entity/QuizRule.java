package com.tuan.exam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.tuan.exam.entity.Subject;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizRule {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private int easyCount;     // Ví dụ: 10 câu Dễ
    private int mediumCount;   // Ví dụ: 5 câu Trung bình
    private int hardCount;     // Ví dụ: 3 câu Khó
}

