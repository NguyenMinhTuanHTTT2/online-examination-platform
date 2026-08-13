package com.tuan.exam.service.impl;

import com.tuan.exam.entity.Quiz;
import com.tuan.exam.entity.QuizAttempt;
import com.tuan.exam.entity.enums.AttemptStatus;
import com.tuan.exam.repository.QuizAttemptRepository;
import com.tuan.exam.repository.QuizRepository;
import com.tuan.exam.service.interfaces.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor

public class ExcelExportServiceImpl implements ExcelExportService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;

    @Override
    public ByteArrayInputStream exportQuizScoresToExcel(Long quizId, String username) {
        // 1. Kiểm tra đề thi và quyền sở hữu
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Đề thi không tồn tại"));

        if (!quiz.getCreator().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền xuất điểm của đề thi này");
        }

        // 2. Lấy danh sách học viên đã nộp bài
    List<QuizAttempt> attempts = attemptRepository.findByQuizIdAndStatus(quizId, AttemptStatus.SUBMITTED);
        int totalMarks = quiz.getTotalMarks() > 0 ? quiz.getTotalMarks() : 1;

        // 3. Khởi tạo Workbook (File Excel) và Sheet
        try (Workbook workbook = new SXSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Bảng Điểm");

            // Style cho Header (In đậm, nền xám)
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 4. Tạo Header Row (Dòng tiêu đề)
            String[] HEADERS = {"STT", "Tài khoản (Username)", "Họ và Tên", "Bắt đầu lúc", "Nộp bài lúc", "Điểm số", "Phần trăm (%)", "Kết quả"};
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 5. Đổ dữ liệu vào các dòng tiếp theo
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            int rowIndex = 1;

            for (QuizAttempt attempt : attempts) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(rowIndex - 1); // STT
                row.createCell(1).setCellValue(attempt.getUser().getUsername());
                row.createCell(2).setCellValue(attempt.getUser().getFullName()); // Giả định User có trường fullName

                // Format thời gian
                String startTimeStr = attempt.getStartTime() != null ? attempt.getStartTime().format(dateFormatter) : "";
                String submitTimeStr = attempt.getSubmitTime() != null ? attempt.getSubmitTime().format(dateFormatter) : "";
                row.createCell(3).setCellValue(startTimeStr);
                row.createCell(4).setCellValue(submitTimeStr);

                // Điểm số và %
                double score = attempt.getScore();
                double percentage = (score / totalMarks) * 100;

                row.createCell(5).setCellValue(Math.round(score * 100.0) / 100.0);
                row.createCell(6).setCellValue(Math.round(percentage * 100.0) / 100.0 + "%");

                // Trạng thái Đạt/Trượt
                String status = Boolean.TRUE.equals(attempt.getIsPassed()) ? "Đạt" : "Không đạt";
                row.createCell(7).setCellValue(status);
            }

            // 6. Tự động căn chỉnh độ rộng các cột (Auto-size)
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 7. Ghi file Excel ra luồng (Stream)
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Lỗi trong quá trình tạo file Excel: " + e.getMessage());
        }
    }
}
