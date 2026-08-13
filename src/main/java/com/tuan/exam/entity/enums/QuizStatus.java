package com.tuan.exam.entity.enums;

public enum QuizStatus {
    DRAFT("Bản nháp"),
    PUBLISHED("Đã xuất bản"),
    ARCHIVED("Đã lưu trữ");

    private final String vietnameseName;

    QuizStatus(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }
}
