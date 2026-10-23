package com.tuan.exam.dto.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonDeserialize(using = SubmitQuizRequest.SubmitQuizRequestDeserializer.class)
public class SubmitQuizRequest {

    private List<QuestionSubmissionDto> answers = new ArrayList<>();

    public SubmitQuizRequest() {}

    public SubmitQuizRequest(List<QuestionSubmissionDto> answers) {
        this.answers = answers != null ? answers : new ArrayList<>();
    }

    // Bộ giải mã thông minh: Xử lý triệt để mọi kiểu JSON từ Frontend gửi lên
    public static class SubmitQuizRequestDeserializer extends JsonDeserializer<SubmitQuizRequest> {
        @Override
        public SubmitQuizRequest deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            JsonNode node = mapper.readTree(p);

            SubmitQuizRequest request = new SubmitQuizRequest();
            List<QuestionSubmissionDto> list = new ArrayList<>();

            if (node == null) {
                return request;
            }

            if (node.isArray()) {
                // Dạng 1: React gửi Mảng trực tiếp [...]
                for (JsonNode item : node) {
                    QuestionSubmissionDto dto = mapper.treeToValue(item, QuestionSubmissionDto.class);
                    if (dto != null) list.add(dto);
                }
            } else if (node.isObject()) {
                // Dạng 2: React gửi Object { "answers": [...] } hoặc { "submissions": [...] }
                JsonNode answersNode = node.has("answers") ? node.get("answers") :
                        (node.has("submissions") ? node.get("submissions") : null);

                if (answersNode != null && answersNode.isArray()) {
                    for (JsonNode item : answersNode) {
                        QuestionSubmissionDto dto = mapper.treeToValue(item, QuestionSubmissionDto.class);
                        if (dto != null) list.add(dto);
                    }
                } else {
                    // Dạng 3: React gửi Map { "questionId": [selectedOptionIds] }
                    node.fields().forEachRemaining(entry -> {
                        try {
                            Long qId = Long.parseLong(entry.getKey());
                            QuestionSubmissionDto dto = new QuestionSubmissionDto();
                            dto.setQuestionId(qId);
                            JsonNode val = entry.getValue();
                            List<Long> selected = new ArrayList<>();
                            if (val.isArray()) {
                                for (JsonNode v : val) {
                                    selected.add(v.asLong());
                                }
                            } else if (val.isNumber()) {
                                selected.add(val.asLong());
                            } else if (val.isTextual()) {
                                dto.setTextAnswer(val.asText());
                            }
                            dto.setSelectedAnswerIds(selected);
                            list.add(dto);
                        } catch (Exception ignored) {}
                    });
                }
            }

            request.setAnswers(list);
            return request;
        }
    }
}