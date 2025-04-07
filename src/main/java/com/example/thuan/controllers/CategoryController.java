package com.example.thuan.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.*;

import com.example.thuan.daos.CategoryDAO;
import com.example.thuan.models.CategoryDTO;
import com.example.thuan.respone.BaseResponse;
import com.example.thuan.respone.CategoryResponse;
import com.example.thuan.respone.Meta;
import com.example.thuan.respone.PaginationResponse;

import io.jsonwebtoken.lang.Arrays;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@EnableWebSecurity
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    CategoryDAO categoryDAO;

    @Autowired
    public CategoryController(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    @GetMapping("/")
    public BaseResponse<CategoryResponse> getCategoriesList() {
        try {
            List<CategoryDTO> categories = categoryDAO.findAll();

            CategoryResponse response = new CategoryResponse();
            response.setCategories(categories.toArray(new CategoryDTO[0]));

            return BaseResponse.success(
                    "Successfully retrieved categories",
                    200,
                    response,
                    null,
                    null);
        } catch (Exception e) {
            return BaseResponse.error(
                    "Failed to retrieve categories: " + e.getMessage(),
                    500,
                    null);
        }
    }

    @GetMapping("/filter")
    public BaseResponse<CategoryResponse> filterCategories(@RequestParam(required = false) String categoryIds) {
        try {
            List<Integer> ids = new ArrayList<>();
            if (categoryIds != null) {
                String[] parts = categoryIds.split(",");
                for (String part : parts) {
                    ids.add(Integer.parseInt(part.trim()));
                }
            }

            List<CategoryDTO> categories = categoryDAO.searchByCategoryIds(ids);

            CategoryResponse response = new CategoryResponse();
            response.setCategories(categories.toArray(new CategoryDTO[0]));

            return BaseResponse.success(
                    "Successfully filtered categories",
                    200,
                    response,
                    null,
                    null);
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error("Failed to filter categories: " + e.getMessage(), 500, null);
        }
    }

    @GetMapping("/category-pagination")
    public BaseResponse<PaginationResponse<CategoryDTO>> getCategoriesPagination(
            @RequestParam(name = "catName", required = false) String catName,
            @RequestParam(name = "catStatus", required = false) String catStatus,
            @RequestParam(name = "current", defaultValue = "1") int current,
            @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(name = "sort", required = false) String sort) {

        try {
            int offset = (current - 1) * pageSize;

            // Gọi DAO để lấy danh sách danh mục theo điều kiện tìm kiếm (catName,
            // catStatus) và sắp xếp
            List<CategoryDTO> data = categoryDAO.getCategories(offset, pageSize, catName, catStatus, sort);

            // Đếm tổng số bản ghi theo điều kiện tìm kiếm
            long total = categoryDAO.countCategoriesWithConditions(catName, catStatus);
            int pages = (pageSize == 0) ? 0 : (int) Math.ceil((double) total / pageSize);

            Meta meta = new Meta();
            meta.setCurrent(current);
            meta.setPageSize(pageSize);
            meta.setPages(pages);
            meta.setTotal(total);

            PaginationResponse<CategoryDTO> pagingRes = new PaginationResponse<>(data, meta);
            return BaseResponse.success("Lấy danh sách danh mục thành công!", 200, pagingRes, null, null);
        } catch (Exception e) {
            return BaseResponse.error("Lỗi: " + e.getMessage(), 500, null);
        }
    }

    @PostMapping("/")
    public BaseResponse<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryDTO newCategory = categoryDAO.save(categoryDTO);
            return BaseResponse.success("Category created successfully!", 200, newCategory, null, null);
        } catch (Exception e) {
            return BaseResponse.error(e.getMessage(), 500, null);
        }
    }

    @PutMapping("/")
    public BaseResponse<CategoryDTO> updateCategory(@RequestBody CategoryDTO categoryDTO) {
        try {
            if (categoryDTO.getCatID() == null) {
                throw new IllegalArgumentException("Category ID is required for update");
            }
            // Gọi DAO để cập nhật danh mục (với merge, nếu entity đã có thì cập nhật)
            CategoryDTO updatedCategory = categoryDAO.save(categoryDTO);
            return BaseResponse.success("Category updated successfully!", 200, updatedCategory, null, null);
        } catch (Exception e) {
            return BaseResponse.error(e.getMessage(), 500, null);
        }
    }

    @DeleteMapping("/{catID}")
    public BaseResponse<Void> deleteCategory(@PathVariable("catID") Integer catID) {
        try {
            categoryDAO.delete(catID);
            return BaseResponse.success("Category deleted successfully!", 200, null, null, null);
        } catch (Exception e) {
            return BaseResponse.error("Failed to delete category: " + e.getMessage(), 500, null);
        }
    }
}