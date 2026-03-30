package com.example.localizationtesting.dto;

public record LanguageRequest(
                String code,
                String name,
                String localeCode,
                boolean rtl,
                boolean active,
                boolean defaultLanguage) {
}