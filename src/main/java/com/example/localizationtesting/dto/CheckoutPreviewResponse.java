package com.example.localizationtesting.dto;

import java.math.BigDecimal;

public record CheckoutPreviewResponse(
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        String currency,
        String formattedSubtotal,
        String formattedDiscount,
        String formattedTotal) {
}
