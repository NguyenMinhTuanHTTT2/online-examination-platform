package com.tuan.exam.entity.enums;

public enum AttemptStatus {
    IN_PROGRESS("Đang làm bài"),
    SUBMITTED("Đã nộp bài"),
    COMPLETED("Đã hoàn thành"),
    TIMED_OUT("Hết giờ"),
    CANCELLED("Đã hủy");

    private final String vietnameseName;

    AttemptStatus(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }
}