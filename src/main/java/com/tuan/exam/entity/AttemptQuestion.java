package com.tuan.exam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attempt_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttemptQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id")
    private QuizAttempt attempt;

    @ManyToOne
    private Question question;

    private int questionOrder; // Thứ tự câu hỏi (1, 2, 3...)

    @Column(name = "option_order")
    private String optionOrder; // Chuỗi lưu ID đáp án đã trộn, ví dụ: "104,101,103,102"

}
