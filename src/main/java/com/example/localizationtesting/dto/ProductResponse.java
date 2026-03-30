package com.example.localizationtesting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductResponse(
                Long id,
                String sku,
                String slug,
                String name,
                String shortDescription,
                String description,
                String nameEn,
                String nameVi,
                String nameFr,
                String nameAr,
                String nameDe,
                String nameJa,
                String nameRu,
                String descriptionEn,
                String descriptionVi,
                String descriptionFr,
                String descriptionAr,
                String descriptionDe,
                String descriptionJa,
                String descriptionRu,
                BigDecimal price,
                BigDecimal convertedPrice,
                String currencyCode,
                String formattedPrice,
                String thumbnailUrl,
                String imageUrl,
                String imageData,
                String imageMimeType,
                LocalDate releaseDate,
                Long categoryId,
                String categoryName) {
}