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

import java.util.List;

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
}