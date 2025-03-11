package com.example.thuan.request;

import java.sql.Date;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePromotionRequest {

    @NotBlank(message = "Tên khuyến mãi không được để trống")
    @Size(max = 100, message = "Tên khuyến mãi không được vượt quá 100 ký tự")
    private String proName;

    @NotBlank(message = "Mã khuyến mãi không được để trống")
    @Pattern(regexp = "^[A-Z0-9]{3,20}$", message = "Mã khuyến mãi chỉ chứa chữ hoa và số, độ dài từ 3-20 ký tự")
    private String proCode;

    @NotNull(message = "Giảm giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giảm giá phải lớn hơn 0")
    private Double discount;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private Date startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Future(message = "Ngày kết thúc phải là ngày trong tương lai")
    private Date endDate;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
    private Integer quantity;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer proStatus;

    // Thêm trường username của người tạo
    @NotBlank(message = "Username người tạo không được để trống")
    private String createdBy;
}