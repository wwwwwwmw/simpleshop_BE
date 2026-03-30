package com.example.localizationtesting.service.impl;

import com.example.localizationtesting.dto.ProductRequest;
import com.example.localizationtesting.dto.ProductQueryResult;
import com.example.localizationtesting.dto.ProductResponse;
import com.example.localizationtesting.entity.Category;
import com.example.localizationtesting.entity.Currency;
import com.example.localizationtesting.entity.Language;
import com.example.localizationtesting.entity.Product;
import com.example.localizationtesting.exception.ResourceNotFoundException;
import com.example.localizationtesting.mapper.ProductMapper;
import com.example.localizationtesting.repository.CategoryRepository;
import com.example.localizationtesting.repository.CurrencyRepository;
import com.example.localizationtesting.repository.LanguageRepository;
import com.example.localizationtesting.repository.ProductRepository;
import com.example.localizationtesting.service.ProductService;
import com.example.localizationtesting.util.CurrencyFormatUtil;
import com.example.localizationtesting.util.LocalizationTextUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final LanguageRepository languageRepository;
    private final CurrencyRepository currencyRepository;

    public ProductServiceImpl(ProductRepository productRepository,
            CategoryRepository categoryRepository,
            LanguageRepository languageRepository,
            CurrencyRepository currencyRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.languageRepository = languageRepository;
        this.currencyRepository = currencyRepository;
    }

    @Override
    public ProductQueryResult getAllProducts(String lang, String currency, String keyword, Long categoryId, int page,
            int size, String sort) {
        Language language = resolveLanguage(lang);
        Currency selectedCurrency = resolveCurrency(currency);
        String normalizedLang = LocalizationTextUtil.normalizeLanguageCode(language.getCode());
        String normalizedKeyword = LocalizationTextUtil.normalizeForSearch(keyword);
        int resolvedPage = Math.max(page, 0);
        int resolvedSize = Math.min(Math.max(size, 1), 100);

        List<Product> filteredProducts = productRepository.findAll()
                .stream()
                .filter(product -> categoryId == null || isSameCategory(product, categoryId))
                .filter(product -> normalizedKeyword.isBlank() || matchesKeyword(product, language, normalizedKeyword))
                .toList();

        List<Product> sortedProducts = filteredProducts.stream()
                .sorted(resolveComparator(sort, normalizedLang))
                .toList();

        int fromIndex = Math.min(resolvedPage * resolvedSize, sortedProducts.size());
        int toIndex = Math.min(fromIndex + resolvedSize, sortedProducts.size());
        List<ProductResponse> pagedItems = sortedProducts.subList(fromIndex, toIndex)
                .stream()
                .map(product -> toLocalizedResponse(product, language, selectedCurrency))
                .toList();

        long totalElements = sortedProducts.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / resolvedSize);

        return new ProductQueryResult(pagedItems, totalElements, totalPages, resolvedPage, resolvedSize);
    }

    @Override
    public ProductResponse getProductById(Long id, String lang, String currency) {
        Language language = resolveLanguage(lang);
        Currency selectedCurrency = resolveCurrency(currency);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        return toLocalizedResponse(product, language, selectedCurrency);
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        product.setSku(request.sku());
        product.setNameEn(request.resolvedNameEn());
        product.setNameVi(request.resolvedNameVi());
        product.setNameFr(request.resolvedNameFr());
        product.setNameAr(request.resolvedNameAr());
        product.setNameDe(request.resolvedNameDe());
        product.setNameJa(request.resolvedNameJa());
        product.setNameRu(request.resolvedNameRu());
        product.setDescriptionEn(request.resolvedDescriptionEn());
        product.setDescriptionVi(request.resolvedDescriptionVi());
        product.setDescriptionFr(request.resolvedDescriptionFr());
        product.setDescriptionAr(request.resolvedDescriptionAr());
        product.setDescriptionDe(request.resolvedDescriptionDe());
        product.setDescriptionJa(request.resolvedDescriptionJa());
        product.setDescriptionRu(request.resolvedDescriptionRu());
        product.setPrice(request.resolvedBasePrice());
        product.setImageUrl(request.imageUrl());
        product.setImageData(request.imageData());
        product.setImageMimeType(request.imageMimeType());
        product.setReleaseDate(request.releaseDate());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));
            product.setCategory(category);
        }

        Product saved = productRepository.save(product);
        return toLocalizedResponse(saved, resolveLanguage("en"), resolveCurrency(""));
    }

    private ProductResponse toLocalizedResponse(Product product, Language language, Currency selectedCurrency) {
        String normalizedLang = LocalizationTextUtil.normalizeLanguageCode(language.getCode());
        BigDecimal convertedPrice = convertPrice(product.getPrice(), selectedCurrency);
        String localizedName = resolveProductName(product, normalizedLang);
        String localizedDescription = resolveProductDescription(product, normalizedLang);
        String shortDescription = shorten(localizedDescription, 120);
        String slug = toSlug(localizedName, product.getId());
        String localizedCategoryName = resolveCategoryName(product.getCategory(), normalizedLang);
        String formattedPrice = CurrencyFormatUtil.format(
                convertedPrice,
                language.getLocaleCode(),
                selectedCurrency.getCode());

        return ProductMapper.toResponse(
                product,
                slug,
                localizedName,
                shortDescription,
                localizedDescription,
                convertedPrice,
                selectedCurrency.getCode(),
                formattedPrice,
                localizedCategoryName);
    }

    private Comparator<Product> resolveComparator(String sort, String normalizedLang) {
        String normalizedSort = sort == null ? "newest" : sort.trim();

        return switch (normalizedSort) {
            case "priceAsc" -> Comparator.comparing(
                    product -> product.getPrice() == null ? BigDecimal.ZERO : product.getPrice());
            case "priceDesc" -> Comparator.comparing(
                    (Product product) -> product.getPrice() == null ? BigDecimal.ZERO : product.getPrice()).reversed();
            case "nameDesc" -> Comparator.comparing(
                    (Product product) -> resolveProductName(product, normalizedLang),
                    String.CASE_INSENSITIVE_ORDER).reversed();
            case "nameAsc" -> Comparator.comparing(
                    (Product product) -> resolveProductName(product, normalizedLang),
                    String.CASE_INSENSITIVE_ORDER);
            case "newest" -> Comparator
                    .comparing((Product product) -> product.getReleaseDate() == null ? LocalDate.MIN
                            : product.getReleaseDate())
                    .reversed()
                    .thenComparing(Product::getId, Comparator.nullsLast(Comparator.reverseOrder()));
            default -> Comparator
                    .comparing((Product product) -> product.getReleaseDate() == null ? LocalDate.MIN
                            : product.getReleaseDate())
                    .reversed()
                    .thenComparing(Product::getId, Comparator.nullsLast(Comparator.reverseOrder()));
        };
    }

    private boolean isSameCategory(Product product, Long categoryId) {
        return product.getCategory() != null && categoryId.equals(product.getCategory().getId());
    }

    private boolean matchesKeyword(Product product, Language language, String keyword) {
        String lang = LocalizationTextUtil.normalizeLanguageCode(language.getCode());
        String name = LocalizationTextUtil.normalizeForSearch(resolveProductName(product, lang));
        String description = LocalizationTextUtil.normalizeForSearch(resolveProductDescription(product, lang));

        return name.contains(keyword) || description.contains(keyword);
    }

    private Language resolveLanguage(String lang) {
        String normalized = LocalizationTextUtil.normalizeLanguageCode(lang);

        return languageRepository.findByCodeIgnoreCase(normalized)
                .filter(Language::isActive)
                .or(() -> languageRepository.findFirstByDefaultLanguageTrue())
                .orElseGet(() -> {
                    Language fallback = new Language();
                    fallback.setCode("en");
                    fallback.setLocaleCode("en-US");
                    fallback.setActive(true);
                    fallback.setRtl(false);
                    fallback.setDefaultLanguage(true);
                    fallback.setName("English");
                    return fallback;
                });
    }

    private Currency resolveCurrency(String currencyCode) {
        String normalized = currencyCode == null ? "" : currencyCode.trim();

        return currencyRepository.findByCodeIgnoreCase(normalized)
                .filter(Currency::isActive)
                .or(() -> currencyRepository.findFirstByDefaultCurrencyTrue())
                .orElseGet(() -> {
                    Currency fallback = new Currency();
                    fallback.setCode("USD");
                    fallback.setName("US Dollar");
                    fallback.setSymbol("$");
                    fallback.setLocaleCode("en-US");
                    fallback.setExchangeRateToDefault(BigDecimal.ONE);
                    fallback.setDecimalDigits(2);
                    fallback.setSymbolPosition("BEFORE");
                    fallback.setActive(true);
                    fallback.setDefaultCurrency(true);
                    return fallback;
                });
    }

    private BigDecimal convertPrice(BigDecimal basePrice, Currency selectedCurrency) {
        if (basePrice == null || selectedCurrency.getExchangeRateToDefault() == null) {
            return BigDecimal.ZERO;
        }

        return basePrice.multiply(selectedCurrency.getExchangeRateToDefault());
    }

    private String resolveProductName(Product product, String normalizedLang) {
        return switch (normalizedLang) {
            case "vi" -> fallback(product.getNameVi(), product.getNameEn());
            case "fr" -> fallback(product.getNameFr(), product.getNameEn());
            case "ar" -> fallback(product.getNameAr(), product.getNameEn());
            case "de" -> fallback(product.getNameDe(), product.getNameEn());
            case "ja" -> fallback(product.getNameJa(), product.getNameEn());
            case "ru" -> fallback(product.getNameRu(), product.getNameEn());
            default -> fallback(product.getNameEn(), product.getNameVi());
        };
    }

    private String resolveProductDescription(Product product, String normalizedLang) {
        return switch (normalizedLang) {
            case "vi" -> fallback(product.getDescriptionVi(), product.getDescriptionEn());
            case "fr" -> fallback(product.getDescriptionFr(), product.getDescriptionEn());
            case "ar" -> fallback(product.getDescriptionAr(), product.getDescriptionEn());
            case "de" -> fallback(product.getDescriptionDe(), product.getDescriptionEn());
            case "ja" -> fallback(product.getDescriptionJa(), product.getDescriptionEn());
            case "ru" -> fallback(product.getDescriptionRu(), product.getDescriptionEn());
            default -> fallback(product.getDescriptionEn(), product.getDescriptionVi());
        };
    }

    private String resolveCategoryName(Category category, String normalizedLang) {
        if (category == null) {
            return "";
        }

        return switch (normalizedLang) {
            case "vi" -> fallback(category.getNameVi(), category.getNameEn());
            case "fr" -> fallback(category.getNameFr(), category.getNameEn());
            case "ar" -> fallback(category.getNameAr(), category.getNameEn());
            case "de" -> fallback(category.getNameDe(), category.getNameEn());
            case "ja" -> fallback(category.getNameJa(), category.getNameEn());
            case "ru" -> fallback(category.getNameRu(), category.getNameEn());
            default -> fallback(category.getNameEn(), category.getNameVi());
        };
    }

    private String fallback(String preferred, String defaultValue) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }

        return defaultValue == null ? "" : defaultValue;
    }

    private String shorten(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, Math.max(0, maxLength - 1)).trim() + "...";
    }

    private String toSlug(String text, Long id) {
        String normalized = LocalizationTextUtil.normalizeForSearch(text)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        if (normalized.isBlank()) {
            return id == null ? "product" : "product-" + id;
        }

        return id == null ? normalized : normalized + "-" + id;
    }
}