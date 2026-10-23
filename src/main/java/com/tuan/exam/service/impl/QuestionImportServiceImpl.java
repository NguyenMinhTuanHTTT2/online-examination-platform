package com.tuan.exam.service.impl;

import com.tuan.exam.entity.Answer;
import com.tuan.exam.entity.Option;
import com.tuan.exam.entity.Question;
import com.tuan.exam.entity.Quiz;
import com.tuan.exam.entity.enums.Difficulty;
import com.tuan.exam.entity.enums.QuestionType;
import com.tuan.exam.repository.QuestionRepository;
import com.tuan.exam.repository.QuizRepository;
import com.tuan.exam.repository.SubjectRepository;
import com.tuan.exam.service.interfaces.QuestionImportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class QuestionImportServiceImpl implements QuestionImportService {
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final DataFormatter dataFormatter = new DataFormatter(); // Dùng để đọc cell an toàn

    @Override
    @Transactional
    public void importQuestionsFromExcel(Long quizId, MultipartFile file) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi với ID: " + quizId));

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<Question> questionsToSave = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // 1. Đọc nội dung câu hỏi
                String content = getSafeCellValue(row.getCell(0));
                if (content.isEmpty()) continue; // Bỏ qua dòng trống

                // 2. Độ khó
                String difficultyStr = getSafeCellValue(row.getCell(1)).toUpperCase();
                Difficulty difficulty = parseDifficulty(difficultyStr);

                // 3. Lời giải
                String explanation = getSafeCellValue(row.getCell(2));

                Question question = new Question();
                question.setContent(content);
                question.setDifficulty(difficulty);
                question.setExplanation(explanation);
                question.setQuiz(quiz);
                question.setQuestionType(QuestionType.SINGLE_CHOICE);

                // 4. Lấy vị trí đáp án đúng (Cột 7 - Index 7)
                String correctIndexStr = getSafeCellValue(row.getCell(7));
                int correctAnswerIndex = 0;
                try {
                    correctAnswerIndex = Integer.parseInt(correctIndexStr) - 1;
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Lỗi tại dòng " + (i + 1) + ": Cột đáp án đúng (Cột 8) phải là số từ 1 đến 4.");
                }

                // 5. Đọc các đáp án (Cột 3, 4, 5, 6)
                List<Option> options = new ArrayList<>();
                for (int j = 0; j < 4; j++) {
                    String optionContent = getSafeCellValue(row.getCell(3 + j));
                    if (!optionContent.isEmpty()) {
                        Option option = new Option();
                        option.setContent(optionContent);
                        option.setIsCorrect(j == correctAnswerIndex);
                        option.setQuestion(question);
                        options.add(option);
                    }
                }

                if (options.isEmpty()) {
                    throw new RuntimeException("Lỗi tại dòng " + (i + 1) + ": Câu hỏi không có đáp án nào.");
                }

                question.setOptions(options);
                questionsToSave.add(question);
            }

            questionRepository.saveAll(questionsToSave);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc file Excel: " + e.getMessage());
        }
    }

    // Hàm đọc cell an toàn không sợ Null hay lỗi kiểu dữ liệu
    private String getSafeCellValue(Cell cell) {
        if (cell == null) return "";
        return dataFormatter.formatCellValue(cell).trim();
    }

    private Difficulty parseDifficulty(String val) {
        if (val.contains("DỄ") || val.contains("EASY")) return Difficulty.EASY;
        if (val.contains("KHÓ") || val.contains("HARD")) return Difficulty.HARD;
        return Difficulty.MEDIUM;
    }
}
