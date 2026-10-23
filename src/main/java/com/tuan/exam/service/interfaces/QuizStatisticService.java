package com.tuan.exam.service.interfaces;

import com.tuan.exam.dto.response.QuizStatisticResponse;

public interface QuizStatisticService {

    /*Dùng để thống kê và tổng kết kết quả
     của một bài thi (thường dành cho giáo viên hoặc admin sau khi kỳ thi kết thúc).*/
    QuizStatisticResponse getQuizStatistics(Long quizId, String username);
}
