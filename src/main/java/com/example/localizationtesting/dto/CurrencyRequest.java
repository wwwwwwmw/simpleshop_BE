package com.example.localizationtesting.dto;

import java.math.BigDecimal;

public record CurrencyRequest(
                String code,
                String name,
                String symbol,
                String localeCode,
                BigDecimal exchangeRateToDefault,
                int decimalDigits,
                String symbolPosition,
                boolean active,
                boolean defaultCurrency) {
}