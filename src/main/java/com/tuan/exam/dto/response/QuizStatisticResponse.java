package com.tuan.exam.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class QuizStatisticResponse {
    private Long quizId;
    private String quizTitle;

    // Thống kê tổng quan
    private int totalAttempts;      // Tổng số lượt thi đã nộp
    private int uniqueParticipants; // Tổng số học viên tham gia (chỉ đếm ID duy nhất)

    // Thống kê điểm số
    private double averageScore;      // Điểm trung bình (trên hệ điểm thực tế)
    private double averagePercentage; // Điểm trung bình (quy ra %)

    // Thống kê đậu/rớt
    private int passedCount;
    private int failedCount;
    private double passRate; // Tỷ lệ đậu (%)

    // Phổ điểm chi tiết
    private List<ScoreDistributionDto> scoreDistribution;
}
