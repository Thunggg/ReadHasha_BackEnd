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
            List<PromotionDTO> promotions = promotionDAO.findAll();
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
}