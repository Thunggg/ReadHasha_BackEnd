package com.example.thuan.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.thuan.models.CategoryDTO;
import com.example.thuan.exceptions.AppException;
import com.example.thuan.ultis.ErrorCode;

import java.util.List;

@Repository
public class CategoryDAOlmpl implements CategoryDAO {
    EntityManager entityManager;

    @Autowired
    public CategoryDAOlmpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Kiểm tra xem tên danh mục đã tồn tại hay chưa
     * 
     * @param catName      Tên danh mục cần kiểm tra
     * @param excludeCatId ID danh mục cần loại trừ (khi cập nhật)
     * @return true nếu tên đã tồn tại, false nếu chưa tồn tại
     */
    public boolean isCategoryNameExists(String catName, Integer excludeCatId) {
        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(c) FROM CategoryDTO c WHERE LOWER(c.catName) = LOWER(:catName) AND c.catStatus != 0");

        if (excludeCatId != null) {
            jpql.append(" AND c.catID != :excludeCatId");
        }

        TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class)
                .setParameter("catName", catName);

        if (excludeCatId != null) {
            query.setParameter("excludeCatId", excludeCatId);
        }

        return query.getSingleResult() > 0;
    }

    @Override
    @Transactional
    public CategoryDTO save(CategoryDTO categoryDTO) {
        if (categoryDTO.getCatName() == null || categoryDTO.getCatName().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }

        // Kiểm tra tên danh mục đã tồn tại chưa
        boolean isUpdate = (categoryDTO.getCatID() != null);
        if (isCategoryNameExists(categoryDTO.getCatName(), isUpdate ? categoryDTO.getCatID() : null)) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTS);
        }

        return entityManager.merge(categoryDTO);
    }

    @Override
    public CategoryDTO find(int catID) {
        CategoryDTO object = entityManager.find(CategoryDTO.class, catID);
        if (object != null) {
            return object;
        } else {
            throw new RuntimeException("Category not found with id: " + catID);
        }
    }

    // @Override
    // @Transactional
    // public void delete(int catID) {
    // entityManager.remove(this.find(catID));
    // }

    @Override
    public List<CategoryDTO> findAll() {
        TypedQuery<CategoryDTO> query = entityManager.createQuery("SELECT c FROM CategoryDTO c WHERE c.catStatus != 0",
                CategoryDTO.class);

        return query.getResultList();
    }

    @Override
    public List<CategoryDTO> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return findAll(); // Nếu không có tên, trả về toàn bộ danh mục
        }
        TypedQuery<CategoryDTO> query = entityManager.createQuery(
                "SELECT c FROM CategoryDTO c WHERE LOWER(c.catName) LIKE LOWER(:name)",
                CategoryDTO.class);
        query.setParameter("name", "%" + name.trim() + "%");
        return query.getResultList();
    }

    @Override
    public List<CategoryDTO> searchByCategoryIds(List<Integer> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return findAll(); // Nếu không có ID nào, trả về tất cả danh mục
        }

        TypedQuery<CategoryDTO> query = entityManager.createQuery(
                "SELECT c FROM CategoryDTO c WHERE c.catID IN :categoryIds",
                CategoryDTO.class);
        query.setParameter("categoryIds", categoryIds);
        return query.getResultList();
    }

    @Override
    public List<CategoryDTO> getCategories(int offset, int pageSize, String catName, String catStatus, String sort) {
        String baseQuery = "SELECT c FROM CategoryDTO c";
        String whereClause = "";

        boolean hasCatName = (catName != null && !catName.trim().isEmpty());
        boolean hasCatStatus = (catStatus != null && !catStatus.trim().isEmpty());

        // Xây dựng điều kiện WHERE dựa trên cả catName và catStatus
        if (hasCatName && hasCatStatus) {
            whereClause = " WHERE LOWER(c.catName) LIKE LOWER(:catName) AND c.catStatus = :catStatus";
        } else if (hasCatName) {
            whereClause = " WHERE LOWER(c.catName) LIKE LOWER(:catName)";
        } else if (hasCatStatus) {
            whereClause = " WHERE c.catStatus = :catStatus";
        }

        String orderClause = "";
        if (sort != null && !sort.trim().isEmpty()) {
            // Ví dụ: sort có dạng "catName" (ASC) hoặc "-catName" (DESC)
            String sortField = sort;
            String sortOrder = "ASC";
            if (sort.startsWith("-")) {
                sortField = sort.substring(1);
                sortOrder = "DESC";
            }
            // Cho phép sort theo các trường: catID, catName, catStatus
            if (!("catID".equals(sortField) || "catName".equals(sortField) || "catStatus".equals(sortField))) {
                sortField = "catID";
            }
            orderClause = " ORDER BY c." + sortField + " " + sortOrder;
        }

        String jpql = baseQuery + whereClause + orderClause;
        TypedQuery<CategoryDTO> query = entityManager.createQuery(jpql, CategoryDTO.class);

        if (hasCatName) {
            query.setParameter("catName", "%" + catName.trim() + "%");
        }
        if (hasCatStatus) {
            // Chuyển đổi catStatus sang kiểu số (Integer)
            query.setParameter("catStatus", Integer.parseInt(catStatus));
        }

        query.setFirstResult(offset);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }

    @Override
    public long countCategoriesWithConditions(String catName, String catStatus) {
        String baseQuery = "SELECT COUNT(c) FROM CategoryDTO c";
        String whereClause = "";

        boolean hasCatName = (catName != null && !catName.trim().isEmpty());
        boolean hasCatStatus = (catStatus != null && !catStatus.trim().isEmpty());

        if (hasCatName && hasCatStatus) {
            whereClause = " WHERE LOWER(c.catName) LIKE LOWER(:catName) AND c.catStatus = :catStatus";
        } else if (hasCatName) {
            whereClause = " WHERE LOWER(c.catName) LIKE LOWER(:catName)";
        } else if (hasCatStatus) {
            whereClause = " WHERE c.catStatus = :catStatus";
        }

        String jpql = baseQuery + whereClause;
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);

        if (hasCatName) {
            query.setParameter("catName", "%" + catName.trim() + "%");
        }
        if (hasCatStatus) {
            query.setParameter("catStatus", Integer.parseInt(catStatus));
        }

        return query.getSingleResult();
    }

    @Override
    @Transactional
    public void delete(int catID) {
        CategoryDTO category = entityManager.find(CategoryDTO.class, catID);
        if (category != null) {
            // Thực hiện xóa mềm bằng cách đặt catStatus = 0 (Không hoạt động)
            category.setCatStatus(0); // 0 = Inactive/Deleted
            entityManager.merge(category);
        } else {
            throw new RuntimeException("Category not found with id: " + catID);
        }
    }

}