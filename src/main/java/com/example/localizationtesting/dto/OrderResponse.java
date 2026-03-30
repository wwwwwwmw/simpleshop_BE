package com.example.localizationtesting.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
                Long id,
                Long orderId,
                String orderCode,
                String customerName,
                String customerEmail,
                String customerPhone,
                String paymentMethod,
                BigDecimal discountBase,
                ShippingAddress shippingAddress,
                String shippingAddressLine,
                String shippingCity,
                String shippingState,
                String shippingPostalCode,
                String customerNote,
                String status,
                LocalDateTime createdAt,
                List<OrderItemResponse> items,
                BigDecimal subtotal,
                BigDecimal discount,
                String currencyCode,
                BigDecimal amountPayable) {

        public record ShippingAddress(
                        String addressLine,
                        String city,
                        String state,
                        String postalCode) {
        }

        public record OrderItemResponse(
                        Long productId,
                        String productName,
                        Integer quantity,
                        BigDecimal unitPrice,
                        BigDecimal lineTotal) {
        }
}