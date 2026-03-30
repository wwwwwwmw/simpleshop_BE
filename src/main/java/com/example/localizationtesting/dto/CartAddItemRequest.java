package com.example.localizationtesting.dto;

public record CartAddItemRequest(
        Long productId,
        Integer quantity) {
}
