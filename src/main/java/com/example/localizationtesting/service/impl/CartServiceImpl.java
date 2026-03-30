package com.example.localizationtesting.service.impl;

import com.example.localizationtesting.dto.CartAddItemRequest;
import com.example.localizationtesting.dto.CartResponse;
import com.example.localizationtesting.dto.CartUpdateItemRequest;
import com.example.localizationtesting.entity.Cart;
import com.example.localizationtesting.entity.CartItem;
import com.example.localizationtesting.entity.Currency;
import com.example.localizationtesting.entity.Language;
import com.example.localizationtesting.entity.Product;
import com.example.localizationtesting.exception.ResourceNotFoundException;
import com.example.localizationtesting.repository.CartRepository;
import com.example.localizationtesting.repository.CurrencyRepository;
import com.example.localizationtesting.repository.LanguageRepository;
import com.example.localizationtesting.repository.ProductRepository;
import com.example.localizationtesting.service.CartService;
import com.example.localizationtesting.util.CurrencyFormatUtil;
import com.example.localizationtesting.util.LocalizationTextUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final LanguageRepository languageRepository;
    private final CurrencyRepository currencyRepository;

    public CartServiceImpl(
            CartRepository cartRepository,
            ProductRepository productRepository,
            LanguageRepository languageRepository,
            CurrencyRepository currencyRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.languageRepository = languageRepository;
        this.currencyRepository = currencyRepository;
    }

    @Override
    public CartResponse getCart(String sessionKey, String lang, String currency) {
        Cart cart = getOrCreateCart(sessionKey);
        return toResponse(cart, lang, currency);
    }

    @Override
    public CartResponse addItem(String sessionKey, String lang, String currency, CartAddItemRequest request) {
        validateSessionKey(sessionKey);
        if (request == null || request.productId() == null) {
            throw new IllegalArgumentException("productId is required.");
        }

        int safeQuantity = request.quantity() == null ? 1 : Math.max(1, request.quantity());
        Cart cart = getOrCreateCart(sessionKey);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.productId()));

        CartItem existing = cart.getItems()
                .stream()
                .filter(item -> item.getProduct() != null && request.productId().equals(item.getProduct().getId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            int currentQty = existing.getQuantity() == null ? 0 : existing.getQuantity();
            existing.setQuantity(currentQty + safeQuantity);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(safeQuantity);
            cart.getItems().add(cartItem);
        }

        Cart saved = cartRepository.save(cart);
        return toResponse(saved, lang, currency);
    }

    @Override
    public CartResponse updateItem(String sessionKey, String lang, String currency, Long itemId,
            CartUpdateItemRequest request) {
        validateSessionKey(sessionKey);
        if (itemId == null) {
            throw new IllegalArgumentException("itemId is required.");
        }

        int quantity = request == null || request.quantity() == null ? 1 : request.quantity();
        Cart cart = getOrCreateCart(sessionKey);

        CartItem target = cart.getItems()
                .stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (quantity <= 0) {
            cart.getItems().remove(target);
        } else {
            target.setQuantity(quantity);
        }

        Cart saved = cartRepository.save(cart);
        return toResponse(saved, lang, currency);
    }

    @Override
    public CartResponse removeItem(String sessionKey, String lang, String currency, Long itemId) {
        validateSessionKey(sessionKey);
        if (itemId == null) {
            throw new IllegalArgumentException("itemId is required.");
        }

        Cart cart = getOrCreateCart(sessionKey);
        boolean removed = cart.getItems().removeIf(item -> itemId.equals(item.getId()));
        if (!removed) {
            throw new ResourceNotFoundException("Cart item not found with id: " + itemId);
        }

        Cart saved = cartRepository.save(cart);
        return toResponse(saved, lang, currency);
    }

    @Override
    public void clear(String sessionKey) {
        validateSessionKey(sessionKey);
        Cart cart = getOrCreateCart(sessionKey);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(String sessionKey) {
        validateSessionKey(sessionKey);
        return cartRepository.findBySessionKeyIgnoreCase(sessionKey)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setSessionKey(sessionKey.trim());
                    return cartRepository.save(cart);
                });
    }

    private CartResponse toResponse(Cart cart, String lang, String currencyCode) {
        Language language = resolveLanguage(lang);
        Currency currency = resolveCurrency(currencyCode);
        int decimalDigits = Math.max(currency.getDecimalDigits(), 0);

        List<CartResponse.CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product == null) {
                continue;
            }

            int quantity = item.getQuantity() == null ? 0 : Math.max(0, item.getQuantity());
            BigDecimal unitPrice = convertPrice(product.getPrice(), currency)
                    .setScale(decimalDigits, RoundingMode.HALF_UP);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity))
                    .setScale(decimalDigits, RoundingMode.HALF_UP);

            subtotal = subtotal.add(lineTotal);

            itemResponses.add(new CartResponse.CartItemResponse(
                    item.getId(),
                    product.getId(),
                    resolveProductName(product, LocalizationTextUtil.normalizeLanguageCode(language.getCode())),
                    product.getImageUrl(),
                    quantity,
                    unitPrice,
                    lineTotal,
                    CurrencyFormatUtil.format(unitPrice, language.getLocaleCode(), currency.getCode()),
                    CurrencyFormatUtil.format(lineTotal, language.getLocaleCode(), currency.getCode())));
        }

        BigDecimal discount = BigDecimal.ZERO.setScale(decimalDigits, RoundingMode.HALF_UP);
        BigDecimal total = subtotal
                .setScale(decimalDigits, RoundingMode.HALF_UP);

        return new CartResponse(
                itemResponses,
                subtotal,
                discount,
                total,
                currency.getCode(),
                CurrencyFormatUtil.format(subtotal, language.getLocaleCode(), currency.getCode()),
                CurrencyFormatUtil.format(discount, language.getLocaleCode(), currency.getCode()),
                CurrencyFormatUtil.format(total, language.getLocaleCode(), currency.getCode()));
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

    private String fallback(String preferred, String defaultValue) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }

        return defaultValue == null ? "" : defaultValue;
    }

    private void validateSessionKey(String sessionKey) {
        if (sessionKey == null || sessionKey.isBlank()) {
            throw new IllegalArgumentException("X-Cart-Session header is required.");
        }
    }
}