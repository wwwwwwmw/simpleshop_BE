package com.example.localizationtesting.service.impl;

import com.example.localizationtesting.dto.CategoryRequest;
import com.example.localizationtesting.dto.CategoryResponse;
import com.example.localizationtesting.entity.Category;
import com.example.localizationtesting.repository.CategoryRepository;
import com.example.localizationtesting.service.CategoryService;
import com.example.localizationtesting.util.LocalizationTextUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryResponse> getAllCategories(String lang) {
        String normalizedLang = LocalizationTextUtil.normalizeLanguageCode(lang);

        return categoryRepository.findAll()
                .stream()
                .map(category -> toResponse(category, normalizedLang))
                .toList();
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = new Category();
        category.setNameEn(request.resolvedNameEn());
        category.setNameVi(request.resolvedNameVi());
        category.setNameFr(request.resolvedNameFr());
        category.setNameAr(request.resolvedNameAr());
        category.setNameDe(request.resolvedNameDe());
        category.setNameJa(request.resolvedNameJa());
        category.setNameRu(request.resolvedNameRu());

        Category saved = categoryRepository.save(category);

        return toResponse(saved, "en");
    }

    private CategoryResponse toResponse(Category category, String normalizedLang) {
        String localizedName = resolveCategoryName(category, normalizedLang);
        return new CategoryResponse(
                category.getId(),
                toSlug(localizedName, category.getId()),
                localizedName,
                category.getNameEn(),
                category.getNameVi(),
                category.getNameFr(),
                category.getNameAr(),
                category.getNameDe(),
                category.getNameJa(),
                category.getNameRu());
    }

    private String resolveCategoryName(Category category, String normalizedLang) {
        return switch (normalizedLang) {
            case "vi" -> fallback(category.getNameVi(), category.getNameEn());
            case "fr" -> fallback(category.getNameFr(), category.getNameEn());
            case "ar" -> fallback(category.getNameAr(), category.getNameEn());
            case "de" -> fallback(category.getNameDe(), category.getNameEn());
            case "ja" -> fallback(category.getNameJa(), category.getNameEn());
            case "ru" -> fallback(category.getNameRu(), category.getNameEn());
            default -> fallback(category.getNameEn(), category.getNameVi());
        };
    }

    private String fallback(String preferred, String defaultValue) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }

        return defaultValue == null ? "" : defaultValue;
    }

    private String toSlug(String text, Long id) {
        String normalized = LocalizationTextUtil.normalizeForSearch(text)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        if (normalized.isBlank()) {
            return id == null ? "category" : "category-" + id;
        }

        return id == null ? normalized : normalized + "-" + id;
    }
}