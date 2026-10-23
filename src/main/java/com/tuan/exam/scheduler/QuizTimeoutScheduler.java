package com.tuan.exam.scheduler;

import com.tuan.exam.entity.QuizAttempt;
import com.tuan.exam.entity.enums.AttemptStatus;
import com.tuan.exam.repository.QuizAttemptRepository;
import com.tuan.exam.service.interfaces.QuizAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizTimeoutScheduler {
    private final QuizAttemptRepository attemptRepository;
    private final QuizAttemptService attemptService;

    // Chạy mỗi 1 phút một lần
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void scanAndSubmitExpiredAttempts() {
        log.info("Running CronJob: Quét các bài thi quá hạn...");

        // Cần viết thêm 1 hàm trong Repository để tìm các bài thi đang IN_PROGRESS
        List<QuizAttempt> inProgressAttempts = attemptRepository.findByStatus(AttemptStatus.IN_PROGRESS);

        for (QuizAttempt attempt : inProgressAttempts) {
            // Kiểm tra an toàn nếu quiz hoặc settings bị null

            if (attempt.getQuiz() == null || attempt.getQuiz().getSettings() == null) continue;
            Integer duration = attempt.getQuiz().getSettings().getDurationMinutes();
            if (duration == null || duration == 0) continue;

            Instant deadline = attempt.getStartTime()
                    .plus(Duration.ofMillis(duration))
                    .plusSeconds(60);// Thêm 1 phút du di

            if (Instant.now().isAfter(deadline)) {
                // Đánh dấu nhanh trạng thái sang SUBMITTED hoặc một trạng thái tạm NGAY LẬP TỨC
                // để vòng lặp sau hoặc cronjob khác không quét trùng nữa
                attempt.setStatus(AttemptStatus.SUBMITTED);
                attempt.setIsForceSubmitted(true);
                attemptRepository.save(attempt); // Khóa ngay lập tức ở DB
                log.info("Phát hiện lượt thi {} đã hết giờ. Đang tự động nộp bài...", attempt.getId());
                try {
                    // Cờ hiệu hệ thống thu bài
                    attempt.setIsForceSubmitted(true);

                    // Gọi hàm nội bộ để chấm điểm bản nháp
                    // (Bạn cần thiết kế hàm forceSubmitDraft trong Service để tái sử dụng logic chấm điểm)
                    attemptService.forceSubmitDraft(attempt);
                } catch (Exception e) {
                    log.error("Lỗi khi tự động thu bài {}: {}", attempt.getId(), e.getMessage());
                }
            }
        }
    }
}
