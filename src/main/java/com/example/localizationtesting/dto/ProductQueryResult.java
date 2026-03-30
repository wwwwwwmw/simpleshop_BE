package com.example.localizationtesting.dto;

import java.util.List;

public record ProductQueryResult(
        List<ProductResponse> items,
        long totalElements,
        int totalPages,
        int page,
        int size) {
}
