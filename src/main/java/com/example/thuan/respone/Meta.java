package com.example.thuan.respone;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Meta {
    private int current;
    private int pageSize;
    private int pages;
    private long total;
}
