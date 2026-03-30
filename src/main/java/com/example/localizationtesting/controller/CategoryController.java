package com.example.localizationtesting.controller;

import com.example.localizationtesting.dto.CategoryRequest;
import com.example.localizationtesting.dto.CategoryResponse;
import com.example.localizationtesting.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> getAllCategories(
            @RequestParam(name = "lang", required = false, defaultValue = "en") String lang) {
        return categoryService.getAllCategories(lang);
    }

    @PostMapping
    public CategoryResponse createCategory(@RequestBody CategoryRequest request) {
        return categoryService.createCategory(request);
    }
}