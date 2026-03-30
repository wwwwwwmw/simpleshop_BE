package com.example.localizationtesting.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        String currency,
        String formattedSubtotal,
        String formattedDiscount,
        String formattedTotal) {

    public record CartItemResponse(
            Long itemId,
            Long productId,
            String name,
            String imageUrl,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal,
            String formattedUnitPrice,
            String formattedLineTotal) {
    }
}
