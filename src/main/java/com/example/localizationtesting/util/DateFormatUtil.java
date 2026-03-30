package com.example.localizationtesting.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateFormatUtil {

    private DateFormatUtil() {
    }

    public static String format(LocalDate date, String localeCode) {
        if (date == null) {
            return "";
        }

        Locale locale = Locale.forLanguageTag(localeCode.replace('_', '-'));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale);
        return date.format(formatter);
    }
}