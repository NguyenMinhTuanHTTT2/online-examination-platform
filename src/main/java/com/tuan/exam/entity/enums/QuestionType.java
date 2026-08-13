package com.tuan.exam.entity.enums;

public enum QuestionType {
    SINGLE_CHOICE("Trắc nghiệm một đáp án"),
    MULTIPLE_CHOICE("Trắc nghiệm nhiều đáp án"),
    ESSAY("Tự luận"),
    TRUE_FALSE("Đúng / Sai");

    private final String vietnameseName;

    QuestionType(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }
}
