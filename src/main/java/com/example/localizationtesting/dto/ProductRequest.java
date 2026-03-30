package com.example.localizationtesting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProductRequest(
                String sku,
                BigDecimal basePrice,
                List<TranslationRequest> translations,
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
                String imageUrl,
                String imageData,
                String imageMimeType,
                LocalDate releaseDate,
                Long categoryId) {

        public String resolvedNameEn() {
                return fallback(resolveTranslationName("en"), nameEn);
        }

        public String resolvedNameVi() {
                return fallback(resolveTranslationName("vi"), nameVi);
        }

        public String resolvedNameFr() {
                return fallback(resolveTranslationName("fr"), nameFr);
        }

        public String resolvedNameAr() {
                return fallback(resolveTranslationName("ar"), nameAr);
        }

        public String resolvedNameDe() {
                return fallback(resolveTranslationName("de"), nameDe);
        }

        public String resolvedNameJa() {
                return fallback(resolveTranslationName("ja"), nameJa);
        }

        public String resolvedNameRu() {
                return fallback(resolveTranslationName("ru"), nameRu);
        }

        public String resolvedDescriptionEn() {
                return fallback(resolveTranslationDescription("en"), descriptionEn);
        }

        public String resolvedDescriptionVi() {
                return fallback(resolveTranslationDescription("vi"), descriptionVi);
        }

        public String resolvedDescriptionFr() {
                return fallback(resolveTranslationDescription("fr"), descriptionFr);
        }

        public String resolvedDescriptionAr() {
                return fallback(resolveTranslationDescription("ar"), descriptionAr);
        }

        public String resolvedDescriptionDe() {
                return fallback(resolveTranslationDescription("de"), descriptionDe);
        }

        public String resolvedDescriptionJa() {
                return fallback(resolveTranslationDescription("ja"), descriptionJa);
        }

        public String resolvedDescriptionRu() {
                return fallback(resolveTranslationDescription("ru"), descriptionRu);
        }

        public BigDecimal resolvedBasePrice() {
                if (basePrice != null) {
                        return basePrice;
                }
                return price;
        }

        private String resolveTranslationName(String languageCode) {
                if (translations == null || translations.isEmpty()) {
                        return "";
                }

                return translations.stream()
                                .filter(item -> item != null && languageCode.equalsIgnoreCase(item.languageCode()))
                                .map(TranslationRequest::name)
                                .findFirst()
                                .orElse("");
        }

        private String resolveTranslationDescription(String languageCode) {
                if (translations == null || translations.isEmpty()) {
                        return "";
                }

                return translations.stream()
                                .filter(item -> item != null && languageCode.equalsIgnoreCase(item.languageCode()))
                                .map(TranslationRequest::description)
                                .findFirst()
                                .orElse("");
        }

        private String fallback(String preferred, String legacy) {
                if (preferred != null && !preferred.isBlank()) {
                        return preferred;
                }

                return legacy;
        }

        public record TranslationRequest(
                        String languageCode,
                        String name,
                        String shortDescription,
                        String description) {
        }
}