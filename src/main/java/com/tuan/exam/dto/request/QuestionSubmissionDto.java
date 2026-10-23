package com.tuan.exam.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Data
public class QuestionSubmissionDto {

    @NotNull(message = "Question ID không được để trống")
    @JsonAlias({"id", "question_id", "qId", "questionId"})
    private Long questionId;

    @JsonAlias({"selectedAnswerIds", "selectedOptionIds", "optionIds", "answerIds", "selectedAnswers"})
    private List<Long> selectedAnswerIds = new ArrayList<>();

    @JsonAlias({"textAnswer", "essayAnswer", "text", "content"})
    private String textAnswer;

    // Tự động chuyển đổi nếu Frontend gửi ID lẻ thay vì Mảng (ví dụ: selectedOptionId: 10)
    @JsonAlias({"selectedAnswerId", "selectedOptionId", "selectedOptionIds", "optionIds", "selectedAnswerIds"})
    @JsonSetter("selectedAnswerId")
    public void setSelectedAnswerIdSingle(Long id) {
        addSingleId(id);
    }

    @JsonSetter("selectedOptionId")
    public void setSelectedOptionIdSingle(Long id) {
        addSingleId(id);
    }

    @JsonSetter("optionId")
    public void setOptionIdSingle(Long id) {
        addSingleId(id);
    }

    private void addSingleId(Long id) {
        if (id != null) {
            if (this.selectedAnswerIds == null) {
                this.selectedAnswerIds = new ArrayList<>();
            }
            if (!this.selectedAnswerIds.contains(id)) {
                this.selectedAnswerIds.add(id);
            }
        }
    }
}