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
}