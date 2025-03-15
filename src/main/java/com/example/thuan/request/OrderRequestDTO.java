package com.example.thuan.request;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class OrderRequestDTO {
    private String username;
    private String address;
    private String phone;
    private List<OrderDetailRequestDTO> details;
    private Integer promotionID;
    private Integer finalPrice;
}
