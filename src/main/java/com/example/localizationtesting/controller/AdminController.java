package com.example.localizationtesting.controller;

import com.example.localizationtesting.dto.AuthMeResponse;
import com.example.localizationtesting.dto.AdminOrderStatusRequest;
import com.example.localizationtesting.dto.CategoryRequest;
import com.example.localizationtesting.dto.CurrencyResponse;
import com.example.localizationtesting.dto.CurrencyRequest;
import com.example.localizationtesting.dto.LanguageRequest;
import com.example.localizationtesting.dto.LanguageResponse;
import com.example.localizationtesting.dto.OrderResponse;
import com.example.localizationtesting.dto.ProductRequest;
import com.example.localizationtesting.dto.ProductQueryResult;
import com.example.localizationtesting.dto.ProductResponse;
import com.example.localizationtesting.dto.CategoryResponse;
import com.example.localizationtesting.entity.Category;
import com.example.localizationtesting.entity.Currency;
import com.example.localizationtesting.entity.Language;
import com.example.localizationtesting.entity.Order;
import com.example.localizationtesting.entity.Product;
import com.example.localizationtesting.exception.ForbiddenException;
import com.example.localizationtesting.exception.ResourceNotFoundException;
import com.example.localizationtesting.repository.CategoryRepository;
import com.example.localizationtesting.repository.CurrencyRepository;
import com.example.localizationtesting.repository.LanguageRepository;
import com.example.localizationtesting.repository.OrderRepository;
import com.example.localizationtesting.repository.ProductRepository;
import com.example.localizationtesting.service.AuthService;
import com.example.localizationtesting.service.CategoryService;
import com.example.localizationtesting.service.OrderService;
import com.example.localizationtesting.service.ProductService;
import com.example.localizationtesting.util.LocalizationTextUtil;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final String ROLE_ADMIN = "ADMIN";
    private final AuthService authService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final LanguageRepository languageRepository;
    private final CurrencyRepository currencyRepository;
    private final OrderRepository orderRepository;

    public AdminController(AuthService authService,
            ProductService productService,
            CategoryService categoryService,
            OrderService orderService,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            LanguageRepository languageRepository,
            CurrencyRepository currencyRepository,
            OrderRepository orderRepository) {
        this.authService = authService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.languageRepository = languageRepository;
        this.currencyRepository = currencyRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/health")
    public String adminHealth(@RequestHeader("Authorization") String authorizationHeader) {
        ensureAdmin(authorizationHeader);
        return "Admin API is running";
    }

    @GetMapping("/orders")
    public List<OrderResponse> getOrders(@RequestHeader("Authorization") String authorizationHeader) {
        ensureAdmin(authorizationHeader);
        return orderService.getAllOrders();
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrderDetail(@RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("id") Long id) {
        ensureAdmin(authorizationHeader);
        return orderService.getOrderByIdForAdmin(id);
    }

    @PutMapping("/orders/{id}/status")
    public OrderResponse updateOrderStatus(@RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("id") Long id,
            @RequestParam(name = "status") String status) {
        return updateOrderStatusInternal(authorizationHeader, id, status);
    }

    @PatchMapping("/orders/{id}/status")
    public OrderResponse patchOrderStatus(@RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("id") Long id,
            @RequestBody AdminOrderStatusRequest request) {
        String nextStatus = request == null ? "" : request.status();
        return updateOrderStatusInternal(authorizationHeader, id, nextStatus);
    }

    private OrderResponse updateOrderStatusInternal(String authorizationHeader, Long id, String status) {
        ensureAdmin(authorizationHeader);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        String normalizedStatus = status == null ? "" : status.trim().toUpperCase();
        validateOrderTransition(order.getStatus(), normalizedStatus);
        order.setStatus(normalizedStatus);
        Order saved = orderRepository.save(order);
        return orderService.getOrderByIdForAdmin(saved.getId());
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getProducts(@RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "lang", required = false, defaultValue = "en") String lang,
            @RequestParam(name = "currency", required = false, defaultValue = "USD") String currency,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size,
            @RequestParam(name = "sort", required = false, defaultValue = "newest") String sort) {
        ensureAdmin(authorizationHeader);
        ProductQueryResult result = productService.getAllProducts(lang, currency, keyword, categoryId, page, size,
                sort);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.totalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.totalPages()));
        headers.add("X-Page", String.valueOf(result.page()));
        headers.add("X-Size", String.valueOf(result.size()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(result.items());
    }

    @PostMapping("/products")
    public ProductResponse createProduct(@RequestHeader("Authorization") String authorizationHeader,
            @RequestBody ProductRequest request) {
        ensureAdmin(authorizationHeader);
        return productService.createProduct(request);
    }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(@RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("id") Long id,
            @RequestBody ProductRequest request) {
        ensureAdmin(authorizationHeader);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        applyProductRequest(product, request);
        Product saved = productRepository.save(product);
        return productService.getProductById(saved.getId(), "en", "USD");
    }

    @DeleteMapping("/products/{id}")
    public void deleteProduct(@RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("id") Long id) {
        ensureAdmin(authorizationHeader);
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> getCategories(@RequestHeader("Authorization") String authorizationHeader) {
        ensureAdmin(authorizationHeader);
        return categoryRepository.findAll().stream().map(this::toCategoryResponse).toList();
    }

    @PostMapping("/categories")
    public CategoryResponse createCategory(@RequestHeader("Authorization") String authorizationHeader,
            @RequestBody CategoryRequest request) {
        ensureAdmin(authorizationHeader);
        return categoryService.createCategory(request);
    }

    @PutMapping("/categories/{id}")
    public CategoryResponse updateCategory(@RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("id") Long id,
            @RequestBody CategoryRequest request) {
        ensureAdmin(authorizationHeader);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setNameEn(request.resolvedNameEn());
        category.setNameVi(request.resolvedNameVi());
        category.setNameFr(request.resolvedNameFr());
        category.setNameAr(request.resolvedNameAr());
        category.setNameDe(request.resolvedNameDe());
        category.setNameJa(request.resolvedNameJa());
        category.setNameRu(request.resolvedNameRu());

        return toCategoryResponse(categoryRepository.save(category));
    }

    @DeleteMapping("/categories/{id}")
    public void deleteCategory(@RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("id") Long id) {
        ensureAdmin(authorizationHeader);
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @GetMapping("/languages")
    public List<LanguageResponse> getLanguages(@RequestHeader("Authorization") String authorizationHeader) {
        ensureAdmin(authorizationHeader);
        return languageRepository.findAll().stream().map(this::toLanguageResponse).toList();
    }

    @PostMapping("/languages")
    public LanguageResponse createLanguage(@RequestHeader("Authorization") String authorizationHeader,
            @RequestBody LanguageRequest request) {
        ensureAdmin(authorizationHeader);

        if (request.defaultLanguage()) {
            languageRepository.findAll().forEach(existing -> {
                if (existing.isDefaultLanguage()) {
                    existing.setDefaultLanguage(false);
                    languageRepository.save(existing);
                }
            });
        }

        Language language = new Language();
        language.setCode(request.code());
        language.setName(request.name());
        language.setLocaleCode(request.localeCode());
        language.setRtl(request.rtl());
        language.setActive(request.active());
        language.setDefaultLanguage(request.defaultLanguage());
        return toLanguageResponse(languageRepository.save(language));
    }

    @PutMapping("/languages/{id}")
    public LanguageResponse updateLanguage(@RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("id") Long id,
            @RequestBody LanguageRequest request) {
        ensureAdmin(authorizationHeader);
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with id: " + id));

        if (language.isDefaultLanguage() && !request.defaultLanguage() && !request.active()) {
            throw new IllegalArgumentException("Cannot deactivate the current default language.");
        }

        if (request.defaultLanguage()) {
            languageRepository.findAll().forEach(existing -> {
                if (!existing.getId().equals(language.getId()) && existing.isDefaultLanguage()) {
                    existing.setDefaultLanguage(false);
                    languageRepository.save(existing);
                }
            });
        }

        language.setCode(request.code());
        language.setName(request.name());
        language.setLocaleCode(request.localeCode());
        language.setRtl(request.rtl());
        language.setActive(request.active());
        language.setDefaultLanguage(request.defaultLanguage());

        return toLanguageResponse(languageRepository.save(language));
    }

    @GetMapping("/currencies")
    public List<CurrencyResponse> getCurrencies(@RequestHeader("Authorization") String authorizationHeader) {
        ensureAdmin(authorizationHeader);
        return currencyRepository.findAll().stream().map(this::toCurrencyResponse).toList();
    }

    @PostMapping("/currencies")
    public CurrencyResponse createCurrency(@RequestHeader("Authorization") String authorizationHeader,
            @RequestBody CurrencyRequest request) {
        ensureAdmin(authorizationHeader);

        if (request.defaultCurrency()) {
            currencyRepository.findAll().forEach(existing -> {
                if (existing.isDefaultCurrency()) {
                    existing.setDefaultCurrency(false);
                    currencyRepository.save(existing);
                }
            });
        }

        Currency currency = new Currency();
        currency.setCode(request.code());
        currency.setName(request.name());
        currency.setSymbol(request.symbol());
        currency.setLocaleCode(request.localeCode());
        currency.setExchangeRateToDefault(request.exchangeRateToDefault());
        currency.setDecimalDigits(request.decimalDigits());
        currency.setSymbolPosition(request.symbolPosition());
        currency.setActive(request.active());
        currency.setDefaultCurrency(request.defaultCurrency());
        return toCurrencyResponse(currencyRepository.save(currency));
    }

    @PutMapping("/currencies/{id}")
    public CurrencyResponse updateCurrency(@RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("id") Long id,
            @RequestBody CurrencyRequest request) {
        ensureAdmin(authorizationHeader);
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + id));

        if (currency.isDefaultCurrency() && !request.defaultCurrency() && !request.active()) {
            throw new IllegalArgumentException("Cannot deactivate the current default currency.");
        }

        if (request.defaultCurrency()) {
            currencyRepository.findAll().forEach(existing -> {
                if (!existing.getId().equals(currency.getId()) && existing.isDefaultCurrency()) {
                    existing.setDefaultCurrency(false);
                    currencyRepository.save(existing);
                }
            });
        }

        currency.setCode(request.code());
        currency.setName(request.name());
        currency.setSymbol(request.symbol());
        currency.setLocaleCode(request.localeCode());
        currency.setExchangeRateToDefault(request.exchangeRateToDefault());
        currency.setDecimalDigits(request.decimalDigits());
        currency.setSymbolPosition(request.symbolPosition());
        currency.setActive(request.active());
        currency.setDefaultCurrency(request.defaultCurrency());

        return toCurrencyResponse(currencyRepository.save(currency));
    }

    private void applyProductRequest(Product product, ProductRequest request) {
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

        if (request.categoryId() == null) {
            product.setCategory(null);
            return;
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));
        product.setCategory(category);
    }

    private void ensureAdmin(String authorizationHeader) {
        AuthMeResponse currentUser = authService.me(authorizationHeader);
        if (!ROLE_ADMIN.equalsIgnoreCase(currentUser.userRole())) {
            throw new ForbiddenException("Admin role is required.");
        }
    }

    private void validateOrderTransition(String currentStatus, String nextStatus) {
        if (nextStatus.isBlank()) {
            throw new IllegalArgumentException("Order status is required.");
        }

        if ("NEW".equals(currentStatus) && ("PROCESSING".equals(nextStatus) || "CANCELLED".equals(nextStatus))) {
            return;
        }
        if ("PROCESSING".equals(currentStatus) && ("DONE".equals(nextStatus) || "CANCELLED".equals(nextStatus))) {
            return;
        }

        if (currentStatus.equals(nextStatus)) {
            return;
        }

        throw new IllegalArgumentException(
                "Invalid order status transition from " + currentStatus + " to " + nextStatus + ".");
    }

    private CategoryResponse toCategoryResponse(Category category) {
        String name = category.getNameEn() == null ? "" : category.getNameEn();
        String slug = LocalizationTextUtil.normalizeForSearch(name)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (slug.isBlank()) {
            slug = "category-" + category.getId();
        }

        return new CategoryResponse(
                category.getId(),
                slug,
                name,
                category.getNameEn(),
                category.getNameVi(),
                category.getNameFr(),
                category.getNameAr(),
                category.getNameDe(),
                category.getNameJa(),
                category.getNameRu());
    }

    private LanguageResponse toLanguageResponse(Language language) {
        return new LanguageResponse(
                language.getId(),
                language.getCode(),
                language.getName(),
                language.getLocaleCode(),
                language.getLocaleCode(),
                language.isRtl(),
                language.isRtl(),
                language.isActive(),
                language.isDefaultLanguage(),
                language.isDefaultLanguage());
    }

    private CurrencyResponse toCurrencyResponse(Currency currency) {
        return new CurrencyResponse(
                currency.getId(),
                currency.getCode(),
                currency.getName(),
                currency.getSymbol(),
                currency.getLocaleCode(),
                currency.getLocaleCode(),
                currency.getExchangeRateToDefault(),
                currency.getDecimalDigits(),
                currency.getSymbolPosition(),
                currency.isActive(),
                currency.isDefaultCurrency(),
                currency.isDefaultCurrency());
    }
}