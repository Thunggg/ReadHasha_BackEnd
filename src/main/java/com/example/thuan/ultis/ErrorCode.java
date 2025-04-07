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
    INVALID_OLD_PASSWORD(1056, "Mật khẩu cũ không đúng!", HttpStatus.UNAUTHORIZED),
    USER_UNVERIFIED_STATUS_OR_INACTIVE_STATUS(1057, "Tài khoản chưa xác thực hoặc đã bị khóa!",
            HttpStatus.UNAUTHORIZED),

    // 2001 - 2005: lỗi phân trang
    PAGE_SIZE_NOT_VALID(2001, "Số lượng phần tử không hợp lệ!", HttpStatus.BAD_REQUEST),

    // 2006-2056: Lỗi liên quan đến sách
    BOOK_ALREADY_EXISTS(2006, "Sách đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE_TYPE(2007, "Định dạng ảnh không hợp lệ", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(2008, "Danh mục không tồn tại", HttpStatus.NOT_FOUND),
    BOOK_CREATION_ERROR(2009, "Lỗi khi tạo sách", HttpStatus.INTERNAL_SERVER_ERROR),
    BOOK_NOT_FOUND(2010, "Không tìm thấy sách", HttpStatus.BAD_REQUEST),
    ISBN_ALREADY_EXISTS(2011, "Mã ISBN đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    CATEGORY_NAME_EXISTS(2012, "Tên danh mục đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),

    // 2057-2070: Lỗi liên quan đến file
    FILE_UPLOAD_ERROR(2057, "Lỗi khi upload file", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_READ_ERROR(2058, "Lỗi khi đọc file", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_ERROR(2059, "Lỗi khi xóa file", HttpStatus.INTERNAL_SERVER_ERROR),

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
