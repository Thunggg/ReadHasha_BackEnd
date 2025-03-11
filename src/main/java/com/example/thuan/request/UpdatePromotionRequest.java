package com.example.thuan.request;

import java.util.Date;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePromotionRequest {

    @NotNull(message = "ID khuyến mãi không được để trống")
    private Integer proID;

    @NotBlank(message = "Tên khuyến mãi không được để trống")
    @Size(max = 100, message = "Tên khuyến mãi không được vượt quá 100 ký tự")
    private String proName;

    @NotNull(message = "Giảm giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giảm giá phải lớn hơn 0")
    private Double discount;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private Date startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private Date endDate;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer quantity;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer proStatus;

    @NotBlank(message = "Username người cập nhật không được để trống")
    private String updatedBy;
}
