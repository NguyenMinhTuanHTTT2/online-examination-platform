package com.tuan.exam.service.impl;

import com.tuan.exam.service.interfaces.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    // Các định dạng ảnh cho phép
    private final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");

    public FileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir) {
        // Khởi tạo đường dẫn lưu file tuyệt đối
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            // Tự động tạo thư mục nếu chưa tồn tại
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Không thể tạo thư mục lưu trữ file.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        // 1. Kiểm tra file rỗng
        if (file.isEmpty()) {
            throw new RuntimeException("File không được để trống");
        }

        // 2. Lấy tên file gốc và kiểm tra đuôi mở rộng
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";

        try {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        } catch (Exception e) {
            throw new RuntimeException("File không có định dạng hợp lệ");
        }

        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new RuntimeException("Chỉ cho phép upload file ảnh (jpg, jpeg, png, gif)");
        }

        // 3. Tạo tên file mới (Dùng UUID để tránh trùng tên khi 2 người cùng up file "hinh1.jpg")
        String newFilename = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // 4. Copy file vào thư mục đích
            Path targetLocation = this.fileStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 5. Trả về đường dẫn URL tĩnh để Frontend truy cập
            return "/uploads/questions/" + newFilename;

        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file " + newFilename + ". Vui lòng thử lại!", ex);
        }
    }
}
