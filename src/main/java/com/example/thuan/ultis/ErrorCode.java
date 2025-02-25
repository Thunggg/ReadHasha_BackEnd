package com.example.thuan.ultis;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // 1001 - 1050: lỗi token
    TOKEN_REVOKED(1001, "Refresh token đã bị thu hồi", HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED(1002, "Refresh token has expired", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1003, "Sai refresh token", HttpStatus.BAD_REQUEST),
    WRONG_TOKEN(1004, "Refresh token không tồn tại!", HttpStatus.NOT_FOUND),

    // 1051- 2000: lỗi user
    USER_OR_PASSWORD_WRONG(1051, "Sai tài khoản hoặc mật khẩu!", HttpStatus.UNAUTHORIZED),
    USER_INACTIVE_STATUS(1052, "Tài khoản đã bị khóa!", HttpStatus.FORBIDDEN),
    USER_UNVERIFIED_STATUS(1053, "Tài khoản chưa xác thực email!", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND(1054, "Không tìm thấy người dùng này!", HttpStatus.NOT_FOUND), // Đã sửa mã trùng lặp
    USER_NOT_UPDATE(1055, "Không thể update admin!", HttpStatus.NOT_FOUND), // Đã sửa mã trùng lặp

    // 2001 - 2005: lỗi phân trang
    PAGE_SIZE_NOT_VALID(2001, "Số lượng phần tử không hợp lệ!", HttpStatus.BAD_REQUEST),

    // Lỗi server
    INTERNAL_ERROR(5000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
