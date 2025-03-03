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

}