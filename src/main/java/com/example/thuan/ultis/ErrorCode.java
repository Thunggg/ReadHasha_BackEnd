package com.example.thuan.ultis;

public enum ErrorCode {
    // 1001 - 1050: là custom lỗi cho token
    TOKEN_REVOKED(1001, "Refresh token đã bị thu hồi"),
    TOKEN_EXPIRED(1002, "Refresh token has expired"),
    INVALID_TOKEN(1003, "Sai refresh token"),
    WRONG_TOKEN(1004, "Refresh token không tồn tại!"),

    // 1051- 2000: là custom lỗi cho user
    USER_NOT_FOUND(1051, "Sai tài khoản hoặc mật khẩu!"),
    USER_INACTIVE_STATUS(1052, "Tài khoản đã bị khóa!"),
    USER_UNVERIFIED_STATUS(1053, "Tài khoản chưa xác thực email!"),

    // 2001 - 2005: là custom lỗi cho phân trang
    PAGE_SIZE_NOT_VALID(2001, "Số Lượng Phần tử được lấy ra không hợp lệ!"),

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
