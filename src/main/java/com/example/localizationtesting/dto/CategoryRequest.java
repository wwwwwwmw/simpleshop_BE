package com.example.localizationtesting.dto;

import java.util.List;

public record CategoryRequest(
        String slug,
        List<TranslationRequest> translations,
        String nameEn,
        String nameVi,
        String nameFr,
        String nameAr,
        String nameDe,
        String nameJa,
        String nameRu) {

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

    private String fallback(String preferred, String legacy) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }

        return legacy;
    }

    public record TranslationRequest(
            String languageCode,
            String name,
            String description) {
    }
}