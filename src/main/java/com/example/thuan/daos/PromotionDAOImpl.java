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
        String jpql = "SELECT p FROM PromotionDTO p";
        return entityManager.createQuery(jpql, PromotionDTO.class)
                .getResultList();
    }

    @Override
    public List<PromotionDTO> searchPromotions(String searchTerm) {
        String jpql = "FROM PromotionDTO WHERE LOWER(proName) LIKE :searchTerm OR LOWER(proCode) LIKE :searchTerm";
        TypedQuery<PromotionDTO> query = entityManager.createQuery(jpql, PromotionDTO.class);
        query.setParameter("searchTerm", "%" + searchTerm.toLowerCase() + "%");
        return query.getResultList();
    }

    @Override
    public List<PromotionDTO> getPromotions(int offset, int pageSize, String proName, String proStatus, String sort) {
        String baseQuery = "SELECT p FROM PromotionDTO p";
        String whereClause = "";

        boolean hasProName = (proName != null && !proName.trim().isEmpty());
        boolean hasProStatus = (proStatus != null && !proStatus.trim().isEmpty());

        // Xây dựng điều kiện WHERE dựa trên proName và proStatus
        if (hasProName && hasProStatus) {
            whereClause = " WHERE LOWER(p.proName) LIKE LOWER(:proName) AND p.proStatus = :proStatus";
        } else if (hasProName) {
            whereClause = " WHERE LOWER(p.proName) LIKE LOWER(:proName)";
        } else if (hasProStatus) {
            whereClause = " WHERE p.proStatus = :proStatus";
        }

        String orderClause = "";
        if (sort != null && !sort.trim().isEmpty()) {
            // Ví dụ: sort có dạng "proName" (ASC) hoặc "-proName" (DESC)
            String sortField = sort;
            String sortOrder = "ASC";
            if (sort.startsWith("-")) {
                sortField = sort.substring(1);
                sortOrder = "DESC";
            }
            // Cho phép sort theo các trường: proID, proName, proStatus, discount,
            // startDate, endDate, quantity
            if (!("proID".equals(sortField) || "proName".equals(sortField) ||
                    "proStatus".equals(sortField) || "discount".equals(sortField) ||
                    "startDate".equals(sortField) || "endDate".equals(sortField) ||
                    "quantity".equals(sortField))) {
                sortField = "proID";
            }
            orderClause = " ORDER BY p." + sortField + " " + sortOrder;
        }

        String jpql = baseQuery + whereClause + orderClause;
        TypedQuery<PromotionDTO> query = entityManager.createQuery(jpql, PromotionDTO.class);

        if (hasProName) {
            query.setParameter("proName", "%" + proName.trim() + "%");
        }
        if (hasProStatus) {
            // Chuyển đổi proStatus sang kiểu số (Integer)
            query.setParameter("proStatus", Integer.parseInt(proStatus));
        }

        query.setFirstResult(offset);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }

    @Override
    public long countPromotionsWithConditions(String proName, String proStatus) {
        String baseQuery = "SELECT COUNT(p) FROM PromotionDTO p";
        String whereClause = "";

        boolean hasProName = (proName != null && !proName.trim().isEmpty());
        boolean hasProStatus = (proStatus != null && !proStatus.trim().isEmpty());

        if (hasProName && hasProStatus) {
            whereClause = " WHERE LOWER(p.proName) LIKE LOWER(:proName) AND p.proStatus = :proStatus";
        } else if (hasProName) {
            whereClause = " WHERE LOWER(p.proName) LIKE LOWER(:proName)";
        } else if (hasProStatus) {
            whereClause = " WHERE p.proStatus = :proStatus";
        }

        String jpql = baseQuery + whereClause;
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);

        if (hasProName) {
            query.setParameter("proName", "%" + proName.trim() + "%");
        }
        if (hasProStatus) {
            query.setParameter("proStatus", Integer.parseInt(proStatus));
        }

        return query.getSingleResult();
    }

}
