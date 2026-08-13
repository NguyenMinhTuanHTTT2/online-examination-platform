package com.tuan.exam.dto.response;

import com.tuan.exam.dto.QuizSettingsDto;
import com.tuan.exam.entity.enums.QuizStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class QuizResponse {
    private Long id;                  // ID định danh duy nhất của bài trắc nghiệm/quiz
    private String title;             // Tiêu đề hoặc tên của bài quiz
    private String description;       // Mô tả chi tiết về nội dung hoặc quy định của bài quiz
    private String code;              // Mã code dùng để tham gia bài quiz (nếu có)
    private QuizStatus status;        // Trạng thái hiện tại của bài quiz (VD: Nháp, Đang mở, Đã đóng...)
    private Integer totalQuestions;   // Tổng số câu hỏi có trong bài quiz
    private Integer totalMarks;       // Tổng số điểm tối đa của toàn bộ bài quiz
    private String creatorName;       // Họ tên hoặc username của người tạo ra bài quiz này
    private LocalDateTime createdAt;  // Thời điểm bản ghi bài quiz được tạo trong hệ thống
    private LocalDateTime updatedAt;  // Thời điểm bản ghi bài quiz được cập nhật/chỉnh sửa gần nhất

    private QuizSettingsDto settings; // Đối tượng chứa các thiết lập chi tiết của quiz (thời gian, số lần thi, xáo câu hỏi,...)
}
