package com.tuan.exam.service.impl;

import com.tuan.exam.dto.response.QuizStatisticResponse;
import com.tuan.exam.dto.response.ScoreDistributionDto;
import com.tuan.exam.entity.QuizAttempt;
import com.tuan.exam.entity.enums.AttemptStatus;
import com.tuan.exam.repository.QuizAttemptRepository;
import com.tuan.exam.repository.QuizRepository;
import com.tuan.exam.service.interfaces.QuizStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.tuan.exam.entity.Quiz;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizStatisticServiceImpl implements QuizStatisticService {
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;

    @Override
    public QuizStatisticResponse getQuizStatistics(Long quizId, String username) {
        // 1. Kiểm tra tồn tại và quyền sở hữu (Chỉ người tạo hoặc ADMIN mới được xem)
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Đề thi không tồn tại"));

        // Nếu hệ thống của bạn có ROLE_ADMIN có thể xem tất cả, hãy bổ sung logic check Role ở đây
        if (!quiz.getCreator().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền xem thống kê của đề thi này");
        }

        // 2. Lấy tất cả các bài đã nộp (SUBMITTED)
        List<QuizAttempt> attempts = attemptRepository.findByQuizIdAndStatus(quizId, AttemptStatus.SUBMITTED);

        int totalAttempts = attempts.size();

        // Xử lý trường hợp chưa có ai thi để tránh lỗi chia cho 0
        if (totalAttempts == 0) {
            return QuizStatisticResponse.builder()
                    .quizId(quiz.getId())
                    .quizTitle(quiz.getTitle())
                    .totalAttempts(0)
                    .uniqueParticipants(0)
                    .averageScore(0.0)
                    .averagePercentage(0.0)
                    .passedCount(0)
                    .failedCount(0)
                    .passRate(0.0)
                    .scoreDistribution(initEmptyDistribution()) // Trả về biểu đồ rỗng (0)
                    .build();
        }

        // 3. Tính toán bằng Java Streams
        // Lấy số lượng người thi duy nhất (unique users)
        long uniqueParticipants = attempts.stream()
                .map(a -> a.getUser().getId())
                .distinct()
                .count();

        // Tính tổng số điểm và số người đậu
        double totalScoreSum = 0;
        int passedCount = 0;

        for (QuizAttempt attempt : attempts) {
            totalScoreSum += attempt.getScore();
            if (Boolean.TRUE.equals(attempt.getIsPassed())) {
                passedCount++;
            }
        }

        double avgScore = totalScoreSum / totalAttempts;
        int totalMarks = quiz.getTotalMarks() > 0 ? quiz.getTotalMarks() : 1;
        double avgPercentage = (avgScore / totalMarks) * 100;
        double passRate = ((double) passedCount / totalAttempts) * 100;

        // 4. Chia phổ điểm (Dựa trên %, không phụ thuộc vào hệ điểm của bài)
        int[] distributionCounts = new int[5]; // 0: 0-20%, 1: 21-40%, 2: 41-60%, 3: 61-80%, 4: 81-100%

        for (QuizAttempt attempt : attempts) {
            double currentPercentage = (attempt.getScore() / totalMarks) * 100;
            if (currentPercentage <= 20) distributionCounts[0]++;
            else if (currentPercentage <= 40) distributionCounts[1]++;
            else if (currentPercentage <= 60) distributionCounts[2]++;
            else if (currentPercentage <= 80) distributionCounts[3]++;
            else distributionCounts[4]++;
        }

        List<ScoreDistributionDto> distributionList = new ArrayList<>();
        distributionList.add(new ScoreDistributionDto("0-20%", distributionCounts[0]));
        distributionList.add(new ScoreDistributionDto("21-40%", distributionCounts[1]));
        distributionList.add(new ScoreDistributionDto("41-60%", distributionCounts[2]));
        distributionList.add(new ScoreDistributionDto("61-80%", distributionCounts[3]));
        distributionList.add(new ScoreDistributionDto("81-100%", distributionCounts[4]));

        // 5. Trả về kết quả (Làm tròn 2 chữ số thập phân cho các con số)
        return QuizStatisticResponse.builder()
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .totalAttempts(totalAttempts)
                .uniqueParticipants((int) uniqueParticipants)
                .averageScore(Math.round(avgScore * 100.0) / 100.0)
                .averagePercentage(Math.round(avgPercentage * 100.0) / 100.0)
                .passedCount(passedCount)
                .failedCount(totalAttempts - passedCount)
                .passRate(Math.round(passRate * 100.0) / 100.0)
                .scoreDistribution(distributionList)
                .build();
    }

    private List<ScoreDistributionDto> initEmptyDistribution() {
        return List.of(
                new ScoreDistributionDto("0-20%", 0),
                new ScoreDistributionDto("21-40%", 0),
                new ScoreDistributionDto("41-60%", 0),
                new ScoreDistributionDto("61-80%", 0),
                new ScoreDistributionDto("81-100%", 0)
        );
    }
}
