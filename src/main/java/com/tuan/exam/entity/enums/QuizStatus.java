package com.tuan.exam.entity.enums;

public enum QuizStatus {
    DRAFT("Bản nháp"),
    PUBLISHED("Đã xuất bản"),
    HIDDEN("Ẩn");

    private final String vietnameseName;

    QuizStatus(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }
}
