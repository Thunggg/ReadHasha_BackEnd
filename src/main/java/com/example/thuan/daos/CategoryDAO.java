package com.example.thuan.daos;

import java.util.List;

import com.example.thuan.models.CategoryDTO;

public interface CategoryDAO {
    CategoryDTO save(CategoryDTO category);

    // CategoryDTO find(int catID);

    // void delete(int catID);

    List<CategoryDTO> findAll();

    // Thêm phương thức này để tìm kiếm category theo tên
    List<CategoryDTO> searchByName(String name);

    public List<CategoryDTO> searchByCategoryIds(List<Integer> categoryIds);

    public List<CategoryDTO> getCategories(int offset, int pageSize, String catName, String catStatus, String sort);

    public long countCategoriesWithConditions(String catName, String catStatus);
}