package com.example.localizationtesting.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(
                String customerName,
                String customerEmail,
                String customerPhone,
                ShippingAddress shippingAddress,
                String shippingAddressLine,
                String shippingCity,
                String shippingState,
                String shippingPostalCode,
                String currencyCode,
                String paymentMethod,
                String customerNote,
                BigDecimal discountBase,
                List<OrderItemRequest> items) {

        public String resolvedAddressLine() {
                if (shippingAddress != null && shippingAddress.addressLine() != null
                                && !shippingAddress.addressLine().isBlank()) {
                        return shippingAddress.addressLine();
                }
                return shippingAddressLine;
        }

        public String resolvedCity() {
                if (shippingAddress != null && shippingAddress.city() != null && !shippingAddress.city().isBlank()) {
                        return shippingAddress.city();
                }
                return shippingCity;
        }

        public String resolvedState() {
                if (shippingAddress != null && shippingAddress.state() != null && !shippingAddress.state().isBlank()) {
                        return shippingAddress.state();
                }
                return shippingState;
        }

        public String resolvedPostalCode() {
                if (shippingAddress != null && shippingAddress.postalCode() != null
                                && !shippingAddress.postalCode().isBlank()) {
                        return shippingAddress.postalCode();
                }
                return shippingPostalCode;
        }

        public String resolvedPaymentMethod() {
                if (paymentMethod == null || paymentMethod.isBlank()) {
                        return "COD";
                }
                return paymentMethod.trim().toUpperCase();
        }

        public String resolvedCurrencyCode() {
                if (currencyCode == null || currencyCode.isBlank()) {
                        return "USD";
                }
                return currencyCode.trim().toUpperCase();
        }

        public record ShippingAddress(
                        String addressLine,
                        String city,
                        String state,
                        String postalCode) {
        }

        public record OrderItemRequest(
                        Long productId,
                        Integer quantity,
                        BigDecimal unitPrice) {
        }
}