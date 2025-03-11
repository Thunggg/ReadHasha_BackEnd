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

    public List<PromotionDTO> getPromotions(int offset, int pageSize, String proName, String proStatus, String sort);

    public long countPromotionsWithConditions(String proName, String proStatus);

}