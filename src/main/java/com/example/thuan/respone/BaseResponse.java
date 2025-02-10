package com.example.thuan.respone;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Loại bỏ các field null trong JSON response
public class BaseResponse<T> {
    private String message;
    private int statusCode;
    private T data;
    private Object error;
    private String access_token;
    private String refresh_token;

    public static <T> BaseResponse<T> success(String message, int statusCode, T data, String access_token,
            String refresh_token) {
        return new BaseResponse<>(message, statusCode, data, null, access_token, refresh_token);
    }

    public static <T> BaseResponse<T> error(String message, int statusCode, Object error) {
        return new BaseResponse<>(message, statusCode, null, error, null, null);
    }
}
