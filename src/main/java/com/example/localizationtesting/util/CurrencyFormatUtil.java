package com.example.localizationtesting.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class CurrencyFormatUtil {

    private CurrencyFormatUtil() {
    }

    public static String format(BigDecimal amount, String localeCode, String currencyCode) {
        if (amount == null) {
            return "";
        }

        Locale locale = Locale.forLanguageTag(localeCode.replace('_', '-'));
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setCurrency(Currency.getInstance(currencyCode));
        return formatter.format(amount);
    }
}