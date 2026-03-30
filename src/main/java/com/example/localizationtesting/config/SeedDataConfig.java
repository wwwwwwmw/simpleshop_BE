package com.example.localizationtesting.config;

import com.example.localizationtesting.entity.Category;
import com.example.localizationtesting.entity.Currency;
import com.example.localizationtesting.entity.Language;
import com.example.localizationtesting.entity.Product;
import com.example.localizationtesting.entity.User;
import com.example.localizationtesting.repository.CategoryRepository;
import com.example.localizationtesting.repository.CurrencyRepository;
import com.example.localizationtesting.repository.LanguageRepository;
import com.example.localizationtesting.repository.ProductRepository;
import com.example.localizationtesting.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
public class SeedDataConfig {

    private static final String DEFAULT_SEED_PASSWORD = "123456";

    @Bean
    CommandLineRunner seedData(
            LanguageRepository languageRepository,
            CurrencyRepository currencyRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        return args -> {
            seedLanguages(languageRepository);
            seedCurrencies(currencyRepository);
            seedCatalog(categoryRepository, productRepository);
            seedUsers(userRepository);
        };
    }

    private void seedLanguages(LanguageRepository languageRepository) {
        upsertLanguage(languageRepository, "vi", "Vietnamese", "vi-VN", false, false);
        upsertLanguage(languageRepository, "en", "English", "en-US", false, true);
        upsertLanguage(languageRepository, "fr", "French", "fr-FR", false, false);
        upsertLanguage(languageRepository, "de", "German", "de-DE", false, false);
        upsertLanguage(languageRepository, "ja", "Japanese", "ja-JP", false, false);
        upsertLanguage(languageRepository, "ru", "Russian", "ru-RU", false, false);
        upsertLanguage(languageRepository, "ar", "Arabic", "ar-SA", true, false);
    }

    private void seedCurrencies(CurrencyRepository currencyRepository) {
        upsertCurrency(currencyRepository, "USD", "US Dollar", "$", "en-US", BigDecimal.ONE, 2, "BEFORE", true);
        upsertCurrency(currencyRepository, "VND", "Vietnamese Dong", "d", "vi-VN", new BigDecimal("25000"), 0, "AFTER",
                false);
        upsertCurrency(currencyRepository, "EUR", "Euro", "EUR", "fr-FR", new BigDecimal("0.92"), 2, "AFTER", false);
        upsertCurrency(currencyRepository, "JPY", "Japanese Yen", "JPY", "ja-JP", new BigDecimal("150"), 0, "AFTER",
                false);
        upsertCurrency(currencyRepository, "RUB", "Russian Ruble", "RUB", "ru-RU", new BigDecimal("90"), 2, "AFTER",
                false);
        upsertCurrency(currencyRepository, "SAR", "Saudi Riyal", "SAR", "ar-SA", new BigDecimal("3.75"), 2, "AFTER",
                false);
    }

    private void seedCatalog(CategoryRepository categoryRepository, ProductRepository productRepository) {
        Map<String, Category> categoryByKey = new LinkedHashMap<>();

        List<CategorySeed> categories = List.of(
                new CategorySeed("clothes", "Clothes", "Quan ao", "Vetements", "Mlabes", "Kleidung", "Fuku", "Odezhda"),
                new CategorySeed("accessories", "Accessories", "Phu kien", "Accessoires", "Ekseswarat", "Accessoires",
                        "Akusesari", "Aksessuary"),
                new CategorySeed("art", "Art", "Nghe thuat", "Art", "Fann", "Kunst", "Ato", "Iskusstvo"),
                new CategorySeed("home-accessories", "Home Accessories", "Phu kien nha", "Accessoires maison",
                        "Mlawhiq almanzil", "Wohnaccessoires", "Homu akusesari", "Tovary dlya doma"));

        for (CategorySeed seed : categories) {
            Category category = upsertCategory(categoryRepository, seed);
            categoryByKey.put(seed.key(), category);
        }

        List<ProductSeed> products = List.of(
                new ProductSeed("Brown_bear_-_Vector_graphics", "Brown Bear Vector Graphics", "art",
                        "Brown_bear_-_Vector_graphics.jpg", new BigDecimal("29.90"), 12),
                new ProductSeed("Brown_bear_cushion", "Brown Bear Cushion", "home-accessories",
                        "Brown_bear_cushion.jpg", new BigDecimal("19.90"), 11),
                new ProductSeed("Brown_bear_cushion_1", "Brown Bear Cushion 1", "home-accessories",
                        "Brown_bear_cushion_1.jpg", new BigDecimal("18.90"), 10),
                new ProductSeed("Customizable_mug", "Customizable Mug", "accessories", "Customizable_mug.jpg",
                        new BigDecimal("12.90"), 9),
                new ProductSeed("Hummingbird_-_Vector_graphics", "Hummingbird Vector Graphics", "art",
                        "Hummingbird_-_Vector_graphics.jpg", new BigDecimal("29.90"), 8),
                new ProductSeed("Hummingbird_cushion", "Hummingbird Cushion", "home-accessories",
                        "Hummingbird_cushion.jpg", new BigDecimal("18.90"), 7),
                new ProductSeed("Hummingbird_cushion_1", "Hummingbird Cushion 1", "home-accessories",
                        "Hummingbird_cushion_1.jpg", new BigDecimal("17.90"), 6),
                new ProductSeed("Hummingbird_printed_sweater", "Hummingbird Printed Sweater", "clothes",
                        "Hummingbird_printed_sweater.jpg", new BigDecimal("39.90"), 5),
                new ProductSeed("Hummingbird_printed_t-shirt", "Hummingbird Printed T-Shirt", "clothes",
                        "Hummingbird_printed_t-shirt.jpg", new BigDecimal("23.90"), 4),
                new ProductSeed("Mountain_fox_-_Vector_graphics", "Mountain Fox Vector Graphics", "art",
                        "Mountain_fox_-_Vector_graphics.jpg", new BigDecimal("29.90"), 3),
                new ProductSeed("Mountain_fox_cushion", "Mountain Fox Cushion", "home-accessories",
                        "Mountain_fox_cushion.jpg", new BigDecimal("18.90"), 2),
                new ProductSeed("Mountain_fox_notebook", "Mountain Fox Notebook", "accessories",
                        "Mountain_fox_notebook.jpg", new BigDecimal("11.90"), 1));

        for (ProductSeed seed : products) {
            Category category = categoryByKey.get(seed.categoryKey());
            if (category == null) {
                continue;
            }
            upsertProduct(productRepository, category, seed);
        }
    }

    private void seedUsers(UserRepository userRepository) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        upsertUser(userRepository, encoder, "Admin User", "admin@example.com", DEFAULT_SEED_PASSWORD, "ADMIN");
        upsertUser(userRepository, encoder, "Customer User", "customer@example.com", DEFAULT_SEED_PASSWORD, "CUSTOMER");
    }

    private void upsertLanguage(
            LanguageRepository languageRepository,
            String code,
            String name,
            String localeCode,
            boolean rtl,
            boolean defaultLanguage) {
        if (defaultLanguage) {
            languageRepository.findAll().forEach(existing -> {
                if (!code.equalsIgnoreCase(existing.getCode()) && existing.isDefaultLanguage()) {
                    existing.setDefaultLanguage(false);
                    languageRepository.save(existing);
                }
            });
        }

        Optional<Language> existing = languageRepository.findByCodeIgnoreCase(code);
        Language language = existing.orElseGet(Language::new);
        language.setCode(code);
        language.setName(name);
        language.setLocaleCode(localeCode);
        language.setRtl(rtl);
        language.setActive(true);
        language.setDefaultLanguage(defaultLanguage);
        languageRepository.save(language);
    }

    private void upsertCurrency(
            CurrencyRepository currencyRepository,
            String code,
            String name,
            String symbol,
            String localeCode,
            BigDecimal exchangeRate,
            int decimalDigits,
            String symbolPosition,
            boolean defaultCurrency) {
        if (defaultCurrency) {
            currencyRepository.findAll().forEach(existing -> {
                if (!code.equalsIgnoreCase(existing.getCode()) && existing.isDefaultCurrency()) {
                    existing.setDefaultCurrency(false);
                    currencyRepository.save(existing);
                }
            });
        }

        Optional<Currency> existing = currencyRepository.findByCodeIgnoreCase(code);
        Currency currency = existing.orElseGet(Currency::new);
        currency.setCode(code);
        currency.setName(name);
        currency.setSymbol(symbol);
        currency.setLocaleCode(localeCode);
        currency.setExchangeRateToDefault(exchangeRate);
        currency.setDecimalDigits(decimalDigits);
        currency.setSymbolPosition(symbolPosition);
        currency.setActive(true);
        currency.setDefaultCurrency(defaultCurrency);
        currencyRepository.save(currency);
    }

    private Category upsertCategory(CategoryRepository categoryRepository, CategorySeed seed) {
        Category category = categoryRepository.findByNameEnIgnoreCase(seed.nameEn())
                .orElseGet(Category::new);

        category.setNameEn(seed.nameEn());
        category.setNameVi(seed.nameVi());
        category.setNameFr(seed.nameFr());
        category.setNameAr(seed.nameAr());
        category.setNameDe(seed.nameDe());
        category.setNameJa(seed.nameJa());
        category.setNameRu(seed.nameRu());

        return categoryRepository.save(category);
    }

    private void upsertProduct(ProductRepository productRepository, Category category, ProductSeed seed) {
        Product product = productRepository.findByImageUrlIgnoreCase("/prestashop/" + seed.imageFile())
                .or(() -> productRepository.findByNameEnIgnoreCase(seed.nameEn()))
                .orElseGet(Product::new);

        String baseDescription = "Seeded from PrestaShop image set: " + seed.imageFile();
        product.setNameEn(seed.nameEn());
        product.setNameVi(seed.nameEn());
        product.setNameFr(seed.nameEn());
        product.setNameAr(seed.imageName());
        product.setNameDe(seed.nameEn());
        product.setNameJa(seed.imageName());
        product.setNameRu(seed.nameEn());
        product.setDescriptionEn(baseDescription);
        product.setDescriptionVi(baseDescription);
        product.setDescriptionFr(baseDescription);
        product.setDescriptionAr(baseDescription);
        product.setDescriptionDe(baseDescription);
        product.setDescriptionJa(baseDescription);
        product.setDescriptionRu(baseDescription);
        product.setPrice(seed.price());
        product.setImageUrl("/prestashop/" + seed.imageFile());
        product.setImageData(buildSeedSvgImageData(seed.nameEn()));
        product.setImageMimeType("image/svg+xml");
        product.setCategory(category);
        product.setReleaseDate(LocalDate.now().minusDays(seed.releaseDaysAgo()));

        productRepository.save(product);
    }

    private String buildSeedSvgImageData(String title) {
        String safeTitle = title == null ? "PrestaShop Product"
                : title.replace("&", "and").replace("<", "(").replace(">", ")");
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"800\" height=\"800\">"
                + "<rect width=\"100%\" height=\"100%\" fill=\"#f6f6f6\"/>"
                + "<text x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" "
                + "font-family=\"Arial\" font-size=\"28\" fill=\"#333\">"
                + safeTitle
                + "</text></svg>";
        return Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
    }

    private void upsertUser(
            UserRepository userRepository,
            BCryptPasswordEncoder encoder,
            String fullName,
            String email,
            String rawPassword,
            String role) {
        Optional<User> existing = userRepository.findByEmailIgnoreCase(email);
        User user = existing.orElseGet(User::new);

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(encoder.encode(rawPassword));
        user.setRole(role);

        userRepository.save(user);
    }

    private record CategorySeed(
            String key,
            String nameEn,
            String nameVi,
            String nameFr,
            String nameAr,
            String nameDe,
            String nameJa,
            String nameRu) {
    }

    private record ProductSeed(
            String imageName,
            String nameEn,
            String categoryKey,
            String imageFile,
            BigDecimal price,
            int releaseDaysAgo) {
    }
}
