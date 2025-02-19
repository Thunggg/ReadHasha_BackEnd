package com.example.thuan.ultis;

public enum ErrorCode {
    TOKEN_REVOKED(1001, "Refresh token has been revoked"),
    TOKEN_EXPIRED(1002, "Refresh token has expired"),
    INVALID_TOKEN(1003, "Invalid refresh token"),
    USER_NOT_FOUND(1004, "User not found"),
    INTERNAL_ERROR(5000, "Internal server error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
