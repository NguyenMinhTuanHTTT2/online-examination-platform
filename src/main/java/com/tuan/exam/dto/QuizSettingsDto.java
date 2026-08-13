package com.tuan.exam.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class QuizSettingsDto {
    private Integer durationMinutes;       // Thời gian làm bài (tính bằng phút)
    private Integer passScorePercentage;   // Tỷ lệ phần trăm điểm tối thiểu để đạt (VD: 80%)
    private Integer maxAttempts;           // Số lần cho phép làm bài tối đa
    private Boolean shuffleQuestions;      // Cờ bật/tắt xáo trộn thứ tự các câu hỏi (true/false)
    private Boolean shuffleAnswers;        // Cờ bật/tắt xáo trộn thứ tự các đáp án trong câu hỏi
    private Boolean showResultImmediately; // Hiển thị kết quả ngay sau khi nộp bài hay không
    private Boolean allowReview;           // Cho phép học viên xem lại bài làm sau khi hoàn thành
    private LocalDateTime startTime;       // Thời gian chính thức mở đề thi
    private LocalDateTime endTime;         // Thời gian chính thức đóng đề thi

}
