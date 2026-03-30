package com.example.localizationtesting.service;

import com.example.localizationtesting.dto.CategoryRequest;
import com.example.localizationtesting.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories(String lang);

    CategoryResponse createCategory(CategoryRequest request);
}