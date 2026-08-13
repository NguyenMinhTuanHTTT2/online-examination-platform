package com.tuan.exam.entity.enums;

public enum UserStatus {
    ACTIVE("Đang hoạt động"),
    INACTIVE("Không hoạt động"),
    BLOCKED("Bị khóa");

    private final String vietnameseName;

    UserStatus(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }
}
