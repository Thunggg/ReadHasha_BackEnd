package com.example.thuan.respone;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // ✅ Loại bỏ các field có giá trị null
public class BaseResponse<T> {
    private String message;
    private int statusCode;
    private T data;
    private Object error;

    public static <T> BaseResponse<T> success(String message, int statusCode, T data) {
        return new BaseResponse<>(message, statusCode, data, null);
    }

    public static <T> BaseResponse<T> error(String message, int statusCode, Object error) {
        return new BaseResponse<>(message, statusCode, null, error);
    }
}