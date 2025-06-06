package com.example.thuan.daos;

import java.util.List;

import com.example.thuan.models.PromotionDTO;

public interface PromotionDAO {
    void save(PromotionDTO promotionDTO);

    PromotionDTO find(int proID);

    void update(PromotionDTO promotionDTO);

    void delete(int proID);

    List<PromotionDTO> findAll();

    List<PromotionDTO> searchPromotions(String searchTerm);

    public List<PromotionDTO> getPromotions(int offset, int pageSize, String proName, String proStatus, String sort,
            String startDate, String endDate);

    public long countPromotionsWithConditions(String proName, String proStatus, String startDate, String endDate);

    // Thêm phương thức để kiểm tra mã khuyến mãi đã tồn tại chưa
    boolean isProCodeExists(String proCode);

    List<PromotionDTO> findActivePromotions();

    // Phương thức để tìm các khuyến mãi đã được sử dụng bởi một người dùng cụ thể
    List<PromotionDTO> findPromotionsUsedByUser(String username);
}