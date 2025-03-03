package com.example.thuan.respone;

import com.example.thuan.models.BookDTO;
import com.example.thuan.models.CategoryDTO;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class BookResponse {
    private BookDTO Books[];
}
