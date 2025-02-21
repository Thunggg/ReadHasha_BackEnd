package com.example.thuan.ultis;

public enum ErrorCode {
    // 1001 - 1050: là custom lỗi cho token
    TOKEN_REVOKED(1001, "Refresh token has been revoked"),
    TOKEN_EXPIRED(1002, "Refresh token has expired"),
    INVALID_TOKEN(1003, "Invalid refresh token"),

    // 1051- 20000: là custom lỗi cho user
    USER_NOT_FOUND(1051, "Sai tài khoản hoặc mật khẩu!"),
    USER_INACTIVE_STATUS(1052, "Tài khoản đã bị khóa!"),
    USER_UNVERIFIED_STATUS(1053, "Tài khoản chưa xác thực email!"),

    // Đây là lỗi sever
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
