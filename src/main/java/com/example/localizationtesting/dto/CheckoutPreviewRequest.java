package com.example.localizationtesting.dto;

public record CheckoutPreviewRequest(
        ShippingAddress shippingAddress,
        String note) {
    public record ShippingAddress(
            String addressLine,
            String city,
            String state,
            String postalCode) {
    }
}
