package com.example.localizationtesting.dto;

public record LanguageResponse(
        Long id,
        String code,
        String name,
        String locale,
        String localeCode,
        boolean isRtl,
        boolean rtl,
        boolean active,
        boolean isDefault,
        boolean defaultLanguage) {
}
