package com.example.thuan.service;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToSqlDateConverter implements Converter<String, java.sql.Date> {

    @Override
    public java.sql.Date convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        try {
            // Sử dụng java.sql.Date.valueOf(), yêu cầu định dạng "YYYY-MM-DD"
            return java.sql.Date.valueOf(source);
        } catch (Exception e) {
            throw new IllegalArgumentException("Định dạng ngày không hợp lệ. Vui lòng sử dụng định dạng YYYY-MM-DD.",
                    e);
        }
    }
}
