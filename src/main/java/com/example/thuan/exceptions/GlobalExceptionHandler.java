package com.example.thuan.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.thuan.respone.BaseResponse;
import com.example.thuan.ultis.ErrorCode;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<BaseResponse<?>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.badRequest().body(
                BaseResponse.error(errorCode.getMessage(), errorCode.getCode(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleGeneralException(Exception ex) {
        return ResponseEntity.internalServerError().body(
                BaseResponse.error("Lỗi hệ thống, vui lòng thử lại sau", 5000, ex.getMessage()));
    }

}
