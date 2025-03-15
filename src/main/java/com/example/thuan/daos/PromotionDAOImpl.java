package com.example.thuan.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.thuan.models.PromotionDTO;

import java.util.Date;
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
    public List<PromotionDTO> getPromotions(int offset, int pageSize, String proName, String proStatus, String sort,
            String startDate, String endDate) {
        String baseQuery = "SELECT p FROM PromotionDTO p";
        String whereClause = "";

        boolean hasProName = (proName != null && !proName.trim().isEmpty());
        boolean hasProStatus = (proStatus != null && !proStatus.trim().isEmpty());
        boolean hasStartDate = (startDate != null && !startDate.trim().isEmpty());
        boolean hasEndDate = (endDate != null && !endDate.trim().isEmpty());

        // Xây dựng điều kiện WHERE dựa trên proName, proStatus, startDate và endDate
        if (hasProName || hasProStatus || hasStartDate || hasEndDate) {
            whereClause = " WHERE";

            if (hasProName) {
                whereClause += " LOWER(p.proName) LIKE LOWER(:proName)";
                if (hasProStatus || hasStartDate || hasEndDate) {
                    whereClause += " AND";
                }
            }

            if (hasProStatus) {
                whereClause += " p.proStatus = :proStatus";
                if (hasStartDate || hasEndDate) {
                    whereClause += " AND";
                }
            }

            if (hasStartDate) {
                whereClause += " p.startDate >= :startDate";
                if (hasEndDate) {
                    whereClause += " AND";
                }
            }

            if (hasEndDate) {
                whereClause += " p.endDate <= :endDate";
            }
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
        if (hasStartDate) {
            try {
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.util.Date parsedStartDate = dateFormat.parse(startDate);
                query.setParameter("startDate", parsedStartDate);
            } catch (Exception e) {
                // Xử lý lỗi nếu định dạng ngày không hợp lệ
                e.printStackTrace();
            }
        }
        if (hasEndDate) {
            try {
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.util.Date parsedEndDate = dateFormat.parse(endDate);
                query.setParameter("endDate", parsedEndDate);
            } catch (Exception e) {
                // Xử lý lỗi nếu định dạng ngày không hợp lệ
                e.printStackTrace();
            }
        }

        query.setFirstResult(offset);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }

    @Override
    public long countPromotionsWithConditions(String proName, String proStatus, String startDate, String endDate) {
        String baseQuery = "SELECT COUNT(p) FROM PromotionDTO p";
        String whereClause = "";

        boolean hasProName = (proName != null && !proName.trim().isEmpty());
        boolean hasProStatus = (proStatus != null && !proStatus.trim().isEmpty());
        boolean hasStartDate = (startDate != null && !startDate.trim().isEmpty());
        boolean hasEndDate = (endDate != null && !endDate.trim().isEmpty());

        // Xây dựng điều kiện WHERE dựa trên proName, proStatus, startDate và endDate
        if (hasProName || hasProStatus || hasStartDate || hasEndDate) {
            whereClause = " WHERE";

            if (hasProName) {
                whereClause += " LOWER(p.proName) LIKE LOWER(:proName)";
                if (hasProStatus || hasStartDate || hasEndDate) {
                    whereClause += " AND";
                }
            }

            if (hasProStatus) {
                whereClause += " p.proStatus = :proStatus";
                if (hasStartDate || hasEndDate) {
                    whereClause += " AND";
                }
            }

            if (hasStartDate) {
                whereClause += " p.startDate >= :startDate";
                if (hasEndDate) {
                    whereClause += " AND";
                }
            }

            if (hasEndDate) {
                whereClause += " p.endDate <= :endDate";
            }
        }

        String jpql = baseQuery + whereClause;
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);

        if (hasProName) {
            query.setParameter("proName", "%" + proName.trim() + "%");
        }
        if (hasProStatus) {
            query.setParameter("proStatus", Integer.parseInt(proStatus));
        }
        if (hasStartDate) {
            try {
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.util.Date parsedStartDate = dateFormat.parse(startDate);
                query.setParameter("startDate", parsedStartDate);
            } catch (Exception e) {
                // Xử lý lỗi nếu định dạng ngày không hợp lệ
                e.printStackTrace();
            }
        }
        if (hasEndDate) {
            try {
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.util.Date parsedEndDate = dateFormat.parse(endDate);
                query.setParameter("endDate", parsedEndDate);
            } catch (Exception e) {
                // Xử lý lỗi nếu định dạng ngày không hợp lệ
                e.printStackTrace();
            }
        }

        return query.getSingleResult();
    }

    @Override
    public boolean isProCodeExists(String proCode) {
        if (proCode == null || proCode.trim().isEmpty()) {
            return false;
        }

        String jpql = "SELECT COUNT(p) FROM PromotionDTO p WHERE p.proCode = :proCode";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("proCode", proCode.trim());

        return query.getSingleResult() > 0;
    }

    @Override
    public List<PromotionDTO> findActivePromotions() {
        Date currentDate = new Date();
        String jpql = "FROM PromotionDTO p WHERE p.proStatus = 1 AND p.endDate > :currentDate AND p.startDate < :currentDate";

        return entityManager.createQuery(jpql, PromotionDTO.class)
                .setParameter("currentDate", currentDate)
                .getResultList();
    }
}
