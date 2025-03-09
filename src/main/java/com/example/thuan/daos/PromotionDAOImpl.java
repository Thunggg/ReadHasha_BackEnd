package com.example.thuan.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.thuan.models.PromotionDTO;

import java.util.List;

@Repository
public class PromotionDAOImpl implements PromotionDAO {
    EntityManager entityManager;

    @Autowired
    public PromotionDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void save(PromotionDTO promotionDTO) {
        entityManager.persist(promotionDTO);
    }

    @Override
    public PromotionDTO find(int proID) {
        // Tìm PromotionDTO bằng ID
        return entityManager.find(PromotionDTO.class, proID);
    }

    @Transactional
    public void update(PromotionDTO promotionDTO) {
        entityManager.merge(promotionDTO);
    }

    @Override
    public void delete(int proID) {
        entityManager.remove(this.find(proID));
    }

    @Override
    public List<PromotionDTO> findAll() {
        TypedQuery<PromotionDTO> query = entityManager.createQuery(
                "SELECT p FROM PromotionDTO p LEFT JOIN FETCH p.createdBy LEFT JOIN FETCH p.approvedBy",
                PromotionDTO.class);
        return query.getResultList();
    }

    @Override
    public List<PromotionDTO> searchPromotions(String searchTerm) {
        String jpql = "FROM PromotionDTO WHERE LOWER(proName) LIKE :searchTerm OR LOWER(proCode) LIKE :searchTerm";
        TypedQuery<PromotionDTO> query = entityManager.createQuery(jpql, PromotionDTO.class);
        query.setParameter("searchTerm", "%" + searchTerm.toLowerCase() + "%");
        return query.getResultList();
    }

}
