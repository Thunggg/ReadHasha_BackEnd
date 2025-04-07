package com.example.thuan.controllers;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.example.thuan.daos.AccountDAO;
import com.example.thuan.daos.PromotionDAO;
import com.example.thuan.models.AccountDTO;
import com.example.thuan.models.PromotionDTO;
import com.example.thuan.request.CreatePromotionRequest;
import com.example.thuan.request.UpdatePromotionRequest;
import com.example.thuan.respone.BaseResponse;
import com.example.thuan.respone.Meta;
import com.example.thuan.respone.PaginationResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/promotions")
public class PromotionController {

    @Autowired
    private final PromotionDAO promotionDAO;
    private final AccountDAO accountDAO;

    public PromotionController(PromotionDAO promotionDAO, AccountDAO accountDAO) {
        this.promotionDAO = promotionDAO;
        this.accountDAO = accountDAO; // Inject AccountDAO
    }

    @GetMapping("/")
    public BaseResponse<List<PromotionDTO>> getAllPromotions() {
        try {
            // Lấy tất cả promotion có trạng thái = 1 và còn hạn sử dụng
            List<PromotionDTO> promotions = promotionDAO.findActivePromotions();

            return BaseResponse.success("Lấy danh sách promotion thành công!", 200, promotions, null, null);
        } catch (Exception e) {
            return BaseResponse.error("Lỗi khi lấy danh sách promotion: " + e.getMessage(), 500, null);
        }
    }

    @GetMapping("/promotion-pagination")
    @Transactional
    public BaseResponse<PaginationResponse<PromotionDTO>> getPromotionPagination(
            @RequestParam(name = "proName", required = false) String proName,
            @RequestParam(name = "proStatus", required = false) String proStatus,
            @RequestParam(name = "current", defaultValue = "1") int current,
            @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate) {
        try {
            int offset = (current - 1) * pageSize;

            // Lấy danh sách promotion theo điều kiện tìm kiếm và sắp xếp
            List<PromotionDTO> data = promotionDAO.getPromotions(offset, pageSize, proName, proStatus, sort, startDate,
                    endDate);

            // Đếm tổng số bản ghi theo điều kiện tìm kiếm
            long total = promotionDAO.countPromotionsWithConditions(proName, proStatus, startDate, endDate);
            int pages = (pageSize == 0) ? 0 : (int) Math.ceil((double) total / pageSize);

            Meta meta = new Meta();
            meta.setCurrent(current);
            meta.setPageSize(pageSize);
            meta.setPages(pages);
            meta.setTotal(total);

            PaginationResponse<PromotionDTO> pagingRes = new PaginationResponse<>(data, meta);
            return BaseResponse.success("Lấy danh sách promotion thành công!", 200, pagingRes, null, null);
        } catch (Exception e) {
            return BaseResponse.error("Lỗi: " + e.getMessage(), 500, null);
        }
    }

    @PostMapping("/create")
    @Transactional
    public BaseResponse<PromotionDTO> createPromotion(@Valid @RequestBody CreatePromotionRequest request) {
        try {
            // Kiểm tra mã khuyến mãi đã tồn tại chưa
            if (promotionDAO.isProCodeExists(request.getProCode())) {
                return BaseResponse.error("Mã khuyến mãi đã tồn tại", 400, null);
            }

            // Kiểm tra ngày bắt đầu phải trước ngày kết thúc
            if (request.getStartDate() != null && request.getEndDate() != null
                    && request.getStartDate().after(request.getEndDate())) {
                return BaseResponse.error("Ngày bắt đầu phải trước ngày kết thúc", 400, null);
            }

            // Kiểm tra người tạo có tồn tại không
            AccountDTO createdBy = accountDAO.findByUsername(request.getCreatedBy());
            if (createdBy == null) {
                return BaseResponse.error("Người tạo không tồn tại", 400, null);
            }

            // Tạo đối tượng PromotionDTO từ request
            PromotionDTO promotion = new PromotionDTO();
            promotion.setProName(request.getProName());
            promotion.setProCode(request.getProCode().toUpperCase()); // Đảm bảo mã luôn viết hoa
            promotion.setDiscount(request.getDiscount());
            promotion.setStartDate(request.getStartDate());
            promotion.setEndDate(request.getEndDate());
            promotion.setQuantity(request.getQuantity());
            promotion.setProStatus(request.getProStatus());
            promotion.setCreatedBy(createdBy);

            // Lưu vào database
            promotionDAO.save(promotion);

            return BaseResponse.success("Tạo khuyến mãi thành công", 201, promotion, null, null);
        } catch (Exception e) {
            return BaseResponse.error("Lỗi khi tạo khuyến mãi: " + e.getMessage(), 500, e.getMessage());
        }
    }

    @PutMapping("/update")
    @Transactional
    public BaseResponse<PromotionDTO> updatePromotion(@Valid @RequestBody UpdatePromotionRequest request) {
        try {
            // Kiểm tra promotion có tồn tại không
            PromotionDTO existingPromotion = promotionDAO.find(request.getProID());
            if (existingPromotion == null) {
                return BaseResponse.error("Không tìm thấy khuyến mãi với ID: " +
                        request.getProID(), 404, null);
            }

            // Kiểm tra ngày bắt đầu phải trước ngày kết thúc
            if (request.getStartDate() != null && request.getEndDate() != null
                    && request.getStartDate().after(request.getEndDate())) {
                return BaseResponse.error("Ngày bắt đầu phải trước ngày kết thúc", 400,
                        null);
            }

            // Kiểm tra người cập nhật có tồn tại không
            AccountDTO updatedBy = accountDAO.findByUsername(request.getUpdatedBy());
            if (updatedBy == null) {
                return BaseResponse.error("Người cập nhật không tồn tại", 400, null);
            }

            // Cập nhật thông tin promotion
            existingPromotion.setProName(request.getProName());
            existingPromotion.setDiscount(request.getDiscount());
            existingPromotion.setStartDate(request.getStartDate());
            existingPromotion.setEndDate(request.getEndDate());
            existingPromotion.setQuantity(request.getQuantity());
            existingPromotion.setProStatus(request.getProStatus());

            // Lưu log cập nhật nếu cần
            // Ví dụ: promotionLogDAO.saveLog(existingPromotion, updatedBy, "UPDATE");

            // Cập nhật vào database
            promotionDAO.update(existingPromotion);

            return BaseResponse.success("Cập nhật khuyến mãi thành công", 200, existingPromotion, null, null);
        } catch (Exception e) {
            return BaseResponse.error("Lỗi khi cập nhật khuyến mãi: " + e.getMessage(), 500, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public BaseResponse<PromotionDTO> getPromotionById(@PathVariable("id") int proID) {
        try {
            PromotionDTO promotion = promotionDAO.find(proID);
            if (promotion == null) {
                return BaseResponse.error("Không tìm thấy khuyến mãi với ID: " + proID, 404, null);
            }
            return BaseResponse.success("Lấy thông tin khuyến mãi thành công", 200, promotion, null, null);
        } catch (Exception e) {
            return BaseResponse.error("Lỗi khi lấy thông tin khuyến mãi: " + e.getMessage(), 500, e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public BaseResponse<PromotionDTO> updatePromotionStatus(
            @PathVariable("id") int proID,
            @RequestParam("status") int status,
            @RequestParam("username") String username) {
        try {
            // Kiểm tra promotion có tồn tại không
            PromotionDTO existingPromotion = promotionDAO.find(proID);
            if (existingPromotion == null) {
                return BaseResponse.error("Không tìm thấy khuyến mãi với ID: " + proID, 404, null);
            }

            // Kiểm tra người cập nhật có tồn tại không
            AccountDTO updatedBy = accountDAO.findByUsername(username);
            if (updatedBy == null) {
                return BaseResponse.error("Người cập nhật không tồn tại", 400, null);
            }

            // Kiểm tra trạng thái hợp lệ (ví dụ: 0 = Inactive, 1 = Active)
            if (status != 0 && status != 1) {
                return BaseResponse.error("Trạng thái không hợp lệ. Chỉ chấp nhận giá trị 0 hoặc 1", 400, null);
            }

            // Cập nhật trạng thái
            existingPromotion.setProStatus(status);

            // Lưu log cập nhật nếu cần
            // Ví dụ: promotionLogDAO.saveLog(existingPromotion, updatedBy,
            // "UPDATE_STATUS");

            // Cập nhật vào database
            promotionDAO.update(existingPromotion);

            return BaseResponse.success("Cập nhật trạng thái khuyến mãi thành công", 200, existingPromotion, null,
                    null);
        } catch (Exception e) {
            return BaseResponse.error("Lỗi khi cập nhật trạng thái khuyến mãi: " + e.getMessage(), 500, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public BaseResponse<PromotionDTO> deletePromotion(
            @PathVariable("id") int proID,
            @RequestParam("username") String username) {
        try {
            // Kiểm tra promotion có tồn tại không
            PromotionDTO existingPromotion = promotionDAO.find(proID);
            if (existingPromotion == null) {
                return BaseResponse.error("Không tìm thấy khuyến mãi với ID: " + proID, 404, null);
            }

            // Kiểm tra người xóa có tồn tại không
            AccountDTO deletedBy = accountDAO.findByUsername(username);
            if (deletedBy == null) {
                return BaseResponse.error("Người xóa không tồn tại", 400, null);
            }

            // Kiểm tra xem promotion đã được sử dụng chưa
            // Nếu đã có đơn hàng sử dụng promotion này, không cho phép xóa
            // if (existingPromotion.getOrderList() != null &&
            // !existingPromotion.getOrderList().isEmpty()) {
            // return BaseResponse.error("Không thể xóa khuyến mãi đã được sử dụng trong đơn
            // hàng", 400, null);
            // }

            // Thực hiện soft delete bằng cách đặt proStatus = 2
            existingPromotion.setProStatus(0); // 2 = Deleted

            // Cập nhật vào database
            promotionDAO.update(existingPromotion);

            return BaseResponse.success("Xóa khuyến mãi thành công", 200, existingPromotion, null, null);
        } catch (Exception e) {
            return BaseResponse.error("Lỗi khi xóa khuyến mãi: " + e.getMessage(), 500, e.getMessage());
        }
    }

    @GetMapping("/check-code")
    public BaseResponse<Boolean> checkPromoCode(@RequestParam("code") String proCode) {
        try {
            boolean exists = promotionDAO.isProCodeExists(proCode);
            if (exists) {
                return BaseResponse.success("Mã khuyến mãi đã tồn tại", 200, true, null, null);
            } else {
                return BaseResponse.success("Mã khuyến mãi có thể sử dụng", 200, false, null, null);
            }
        } catch (Exception e) {
            return BaseResponse.error("Lỗi khi kiểm tra mã khuyến mãi: " + e.getMessage(), 500, e.getMessage());
        }
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActivePromotions() {
        try {
            List<PromotionDTO> promotions = promotionDAO.findActivePromotions();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "statusCode", 200,
                    "message", "Active promotions retrieved successfully",
                    "data", promotions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "statusCode", 500,
                    "message", "Failed to retrieve active promotions: " + e.getMessage()));
        }
    }

    @GetMapping("/used/{username}")
    public ResponseEntity<Map<String, Object>> getPromotionsUsedByUser(@PathVariable("username") String username) {
        try {
            List<PromotionDTO> promotions = promotionDAO.findPromotionsUsedByUser(username);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "statusCode", 200,
                    "message", "User's used promotions retrieved successfully",
                    "data", promotions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "statusCode", 500,
                    "message", "Failed to retrieve user's used promotions: " + e.getMessage()));
        }
    }
}