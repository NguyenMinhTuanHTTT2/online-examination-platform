package com.tuan.exam.dto.response;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResultDto {
    private Long questionId;
    private String questionContent;
    private String imageUrl;         // Bổ sung: Đường dẫn ảnh đính kèm câu hỏi
    private Boolean isCorrect;
    private Double scoreEarned;      // Bổ sung: Điểm đạt được (dùng Double để hỗ trợ điểm lẻ)
    private Double maxScore;         // Bổ sung: Điểm tối đa của câu hỏi

    private List<Long> studentAnswerIds; // Danh sách ID đáp án sinh viên đã chọn
    private List<Long> correctAnswerIds; // Danh sách ID đáp án đúng

    private List<OptionDto> options; // Bổ sung: Danh sách lựa chọn (A, B, C, D) phục vụ hiển thị
    private String explanation;      // Lời giải chi tiết
}
