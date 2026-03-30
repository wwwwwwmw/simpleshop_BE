package com.example.localizationtesting.dto;

import java.math.BigDecimal;

public record CurrencyResponse(
        Long id,
        String code,
        String name,
        String symbol,
        String locale,
        String localeCode,
        BigDecimal exchangeRateToDefault,
        Integer decimalDigits,
        String symbolPosition,
        boolean active,
        boolean isDefault,
        boolean defaultCurrency) {
}
