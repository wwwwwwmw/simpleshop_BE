package com.example.localizationtesting.mapper;

import com.example.localizationtesting.dto.ProductResponse;
import com.example.localizationtesting.entity.Product;

import java.math.BigDecimal;

public class ProductMapper {

    private ProductMapper() {
    }

    public static ProductResponse toResponse(
            Product product,
            String slug,
            String localizedName,
            String shortDescription,
            String localizedDescription,
            BigDecimal convertedPrice,
            String currencyCode,
            String formattedPrice,
            String localizedCategoryName) {
        Long categoryId = product.getCategory() != null ? product.getCategory().getId() : null;

        return new ProductResponse(
                product.getId(),
                product.getSku(),
                slug,
                localizedName,
                shortDescription,
                localizedDescription,
                product.getNameEn(),
                product.getNameVi(),
                product.getNameFr(),
                product.getNameAr(),
                product.getNameDe(),
                product.getNameJa(),
                product.getNameRu(),
                product.getDescriptionEn(),
                product.getDescriptionVi(),
                product.getDescriptionFr(),
                product.getDescriptionAr(),
                product.getDescriptionDe(),
                product.getDescriptionJa(),
                product.getDescriptionRu(),
                product.getPrice(),
                convertedPrice,
                currencyCode,
                formattedPrice,
                product.getImageUrl(),
                product.getImageUrl(),
                product.getImageData(),
                product.getImageMimeType(),
                product.getReleaseDate(),
                categoryId,
                localizedCategoryName);
    }
}