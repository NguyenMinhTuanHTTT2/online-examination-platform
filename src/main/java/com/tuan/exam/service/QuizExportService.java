package com.tuan.exam.service;

import com.tuan.exam.entity.QuizResult;
import com.tuan.exam.repository.QuizResultRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class QuizExportService {
    private final QuizResultRepository quizResultRepository;

    @Transactional(readOnly = true) // Bắt buộc phải có để Stream DB không bị đóng giữa chừng
    public void exportScoresToExcelLargeScale(Long quizId, OutputStream outputStream) throws IOException {

        // 1. Tối ưu SXSSFWorkbook: Chỉ giữ 100 dòng trên RAM, nén file tạm
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);

        try {
            Sheet sheet = workbook.createSheet("Bảng Điểm Thí Sinh");

            // 2. KHÔNG LỖI 400: Set độ rộng cố định
            sheet.setColumnWidth(0, 8 * 256);
            sheet.setColumnWidth(1, 20 * 256);
            sheet.setColumnWidth(2, 30 * 256);
            sheet.setColumnWidth(3, 15 * 256);
            sheet.setColumnWidth(4, 18 * 256);
            sheet.setColumnWidth(5, 15 * 256);

            // 3. KHÔNG TRÀN STYLE: Khởi tạo dùng chung
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook, HorizontalAlignment.LEFT);
            CellStyle centerStyle = createDataStyle(workbook, HorizontalAlignment.CENTER);
            CellStyle passStyle = createStatusStyle(workbook, IndexedColors.GREEN.getIndex());
            CellStyle failStyle = createStatusStyle(workbook, IndexedColors.RED.getIndex());

            // 4. Tạo Header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"STT", "Username", "Họ và Tên", "Điểm Số", "Điểm Tối Đa", "Kết Quả"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 5. TỐI ƯU CỰC ĐẠI: Dùng Stream lặp dữ liệu từ DB đẩy thẳng vào Excel
            AtomicInteger rowIndex = new AtomicInteger(1);

            try (Stream<QuizResult> resultStream = quizResultRepository.streamByQuizId(quizId)) {
                resultStream.forEach(res -> {
                    Row row = sheet.createRow(rowIndex.getAndIncrement());

                    Cell cell0 = row.createCell(0); cell0.setCellValue(rowIndex.get() - 1); cell0.setCellStyle(centerStyle);

                    Cell cell1 = row.createCell(1); cell1.setCellValue(res.getUser() != null ? res.getUser().getUsername() : ""); cell1.setCellStyle(dataStyle);

                    Cell cell2 = row.createCell(2); cell2.setCellValue(res.getUser() != null ? res.getUser().getFullName() : ""); cell2.setCellStyle(dataStyle);

                    Cell cell3 = row.createCell(3); cell3.setCellValue(res.getTotalScore()); cell3.setCellStyle(centerStyle);

                    Cell cell4 = row.createCell(4); cell4.setCellValue(res.getMaxPossibleScore()); cell4.setCellStyle(centerStyle);

                    Cell cell5 = row.createCell(5);
                    boolean isPassed = res.getTotalScore() >= (res.getMaxPossibleScore() * 0.5);
                    cell5.setCellValue(isPassed ? "ĐẠT" : "RỚT");
                    cell5.setCellStyle(isPassed ? passStyle : failStyle);
                });
            }

            // 6. Ghi thẳng ra luồng mạng (Network stream) thay vì lưu vào RAM
            workbook.write(outputStream);

        } finally {
            // 7. KHÔNG TRÀN Ổ CỨNG: Luôn luôn dispose
            workbook.dispose();
            workbook.close();
        }
    }

    // Các hàm phụ trợ tạo Style giữ nguyên như bản cũ...
    private CellStyle createHeaderStyle(Workbook workbook) { /* ... */ return workbook.createCellStyle(); }
    private CellStyle createDataStyle(Workbook workbook, HorizontalAlignment alignment) { /* ... */ return workbook.createCellStyle(); }
    private CellStyle createStatusStyle(Workbook workbook, short colorIndex) { /* ... */ return workbook.createCellStyle(); }
}
