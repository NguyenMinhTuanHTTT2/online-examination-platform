package com.tuan.exam.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScoreDistributionDto {
    private String range; // Khoảng điểm (Ví dụ: "0-20%", "21-40%")
    private int count;    // Số lượng bài thi rơi vào khoảng này
}
