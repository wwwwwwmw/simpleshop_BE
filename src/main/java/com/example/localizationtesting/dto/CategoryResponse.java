package com.example.localizationtesting.dto;

public record CategoryResponse(
        Long id,
        String slug,
        String name,
        String nameEn,
        String nameVi,
        String nameFr,
        String nameAr,
        String nameDe,
        String nameJa,
        String nameRu) {
}