package com.tuan.exam.repository;

import com.tuan.exam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    // Dùng cho quá trình Login
    Optional<User> findByUsername(String username);

    // Hỗ trợ quên mật khẩu / Đăng nhập bằng Email
    Optional<User> findByEmail(String email);

    // Kiểm tra dữ liệu khi Đăng ký (Register)
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
