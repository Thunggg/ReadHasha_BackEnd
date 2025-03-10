package com.example.thuan.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.thuan.models.CategoryDTO;

import java.util.List;

@Repository
public class CategoryDAOlmpl implements CategoryDAO {
    EntityManager entityManager;

    @Autowired
    public CategoryDAOlmpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public CategoryDTO save(CategoryDTO categoryDTO) {
        if (categoryDTO.getCatName() == null || categoryDTO.getCatName().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        return entityManager.merge(categoryDTO);
    }

    // @Override
    // public CategoryDTO find(int catID) {
    // CategoryDTO object = entityManager.find(CategoryDTO.class, catID);
    // if (object != null) {
    // return object;
    // } else {
    // throw new CategoryExceptionNotFound();
    // }
    // }

    // @Override
    // @Transactional
    // public void delete(int catID) {
    // entityManager.remove(this.find(catID));
    // }

    @Override
    public List<CategoryDTO> findAll() {
        TypedQuery<CategoryDTO> query = entityManager.createQuery("SELECT c FROM CategoryDTO c", CategoryDTO.class);

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
    public List<CategoryDTO> getCategories(int offset, int pageSize, String catName, String sort) {
        String baseQuery = "SELECT c FROM CategoryDTO c";
        String whereClause = "";
        if (catName != null && !catName.trim().isEmpty()) {
            whereClause = " WHERE LOWER(c.catName) LIKE LOWER(:catName)";
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
            // Chỉ cho phép sort theo các trường nhất định: catID, catName, catStatus
            if (!("catID".equals(sortField) || "catName".equals(sortField) || "catStatus".equals(sortField))) {
                sortField = "catID";
            }
            orderClause = " ORDER BY c." + sortField + " " + sortOrder;
        }
        String jpql = baseQuery + whereClause + orderClause;
        TypedQuery<CategoryDTO> query = entityManager.createQuery(jpql, CategoryDTO.class);
        if (!whereClause.isEmpty()) {
            query.setParameter("catName", "%" + catName.trim() + "%");
        }
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }

    @Override
    public long countCategoriesWithConditions(String catName) {
        String baseQuery = "SELECT COUNT(c) FROM CategoryDTO c";
        String whereClause = "";
        if (catName != null && !catName.trim().isEmpty()) {
            whereClause = " WHERE LOWER(c.catName) LIKE LOWER(:catName)";
        }
        String jpql = baseQuery + whereClause;
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        if (!whereClause.isEmpty()) {
            query.setParameter("catName", "%" + catName.trim() + "%");
        }
        return query.getSingleResult();
    }

}