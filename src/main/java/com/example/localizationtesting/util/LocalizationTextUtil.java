package com.example.localizationtesting.util;

import java.text.Normalizer;
import java.util.Locale;

public final class LocalizationTextUtil {

    private LocalizationTextUtil() {
    }

    public static String normalizeLanguageCode(String lang) {
        if (lang == null || lang.isBlank()) {
            return "en";
        }

        String normalized = lang.toLowerCase(Locale.ROOT);
        int separatorIndex = Math.max(normalized.indexOf('-'), normalized.indexOf('_'));

        if (separatorIndex > 0) {
            return normalized.substring(0, separatorIndex);
        }

        return normalized;
    }

    public static String normalizeForSearch(String input) {
        if (input == null) {
            return "";
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');

        return normalized.toLowerCase(Locale.ROOT).trim();
    }
}
