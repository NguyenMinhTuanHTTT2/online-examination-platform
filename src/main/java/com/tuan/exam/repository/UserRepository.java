package com.tuan.exam.repository;

import com.tuan.exam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Dùng cho quá trình Login
    Optional<User> findByUsername(String username);

    // Hỗ trợ quên mật khẩu / Đăng nhập bằng Email
    Optional<User> findByEmail(String email);

    // 🟢 BỔ SUNG: Tìm kiếm khớp Email HOẶC Username để giải quyết lỗi 400
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.username = :identifier")
    Optional<User> findByEmailOrUsername(@Param("identifier") String identifier);

    // Kiểm tra dữ liệu khi Đăng ký (Register)
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}