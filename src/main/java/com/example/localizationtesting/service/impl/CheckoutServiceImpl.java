package com.example.localizationtesting.service.impl;

import com.example.localizationtesting.dto.CheckoutPreviewRequest;
import com.example.localizationtesting.dto.CheckoutPreviewResponse;
import com.example.localizationtesting.entity.Cart;
import com.example.localizationtesting.entity.CartItem;
import com.example.localizationtesting.entity.Currency;
import com.example.localizationtesting.entity.Language;
import com.example.localizationtesting.repository.CartRepository;
import com.example.localizationtesting.repository.CurrencyRepository;
import com.example.localizationtesting.repository.LanguageRepository;
import com.example.localizationtesting.service.CheckoutService;
import com.example.localizationtesting.util.CurrencyFormatUtil;
import com.example.localizationtesting.util.LocalizationTextUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private final CartRepository cartRepository;
    private final LanguageRepository languageRepository;
    private final CurrencyRepository currencyRepository;

    public CheckoutServiceImpl(
            CartRepository cartRepository,
            LanguageRepository languageRepository,
            CurrencyRepository currencyRepository) {
        this.cartRepository = cartRepository;
        this.languageRepository = languageRepository;
        this.currencyRepository = currencyRepository;
    }

    @Override
    public CheckoutPreviewResponse preview(String cartSession, CheckoutPreviewRequest request, String lang,
            String currency) {
        Language language = resolveLanguage(lang);
        Currency selectedCurrency = resolveCurrency(currency);
        int decimalDigits = Math.max(selectedCurrency.getDecimalDigits(), 0);

        validateSessionKey(cartSession);
        validateAddress(request, language.getCode());
        Cart cart = cartRepository.findBySessionKeyIgnoreCase(cartSession.trim())
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for provided session."));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty.");
        }

        BigDecimal subtotalBase = cart.getItems().stream()
                .map(this::toBaseLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotal = subtotalBase.multiply(selectedCurrency.getExchangeRateToDefault())
                .setScale(decimalDigits, RoundingMode.HALF_UP);

        BigDecimal discount = BigDecimal.ZERO.setScale(decimalDigits, RoundingMode.HALF_UP);

        BigDecimal total = subtotal
                .setScale(decimalDigits, RoundingMode.HALF_UP);

        String localeCode = language.getLocaleCode();
        String currencyCode = selectedCurrency.getCode();

        return new CheckoutPreviewResponse(
                subtotal,
                discount,
                total,
                currencyCode,
                CurrencyFormatUtil.format(subtotal, localeCode, currencyCode),
                CurrencyFormatUtil.format(discount, localeCode, currencyCode),
                CurrencyFormatUtil.format(total, localeCode, currencyCode));
    }

    private BigDecimal toBaseLineTotal(CartItem item) {
        if (item == null || item.getProduct() == null) {
            return BigDecimal.ZERO;
        }

        int quantity = item.getQuantity() == null ? 0 : Math.max(0, item.getQuantity());
        BigDecimal unitPrice = item.getProduct().getPrice() == null ? BigDecimal.ZERO : item.getProduct().getPrice();
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private void validateAddress(CheckoutPreviewRequest request, String languageCode) {
        if (request == null || request.shippingAddress() == null) {
            throw new IllegalArgumentException("Shipping address is required.");
        }

        CheckoutPreviewRequest.ShippingAddress address = request.shippingAddress();

        if (isBlank(address.addressLine()) || isBlank(address.city())) {
            throw new IllegalArgumentException("Address line and city are required.");
        }

        if ("en".equalsIgnoreCase(languageCode)) {
            if (isBlank(address.state())) {
                throw new IllegalArgumentException("State is required for en-US locale.");
            }

            if (isBlank(address.postalCode()) || !address.postalCode().matches("^\\d{5}$")) {
                throw new IllegalArgumentException("Postal code must be 5 digits for en-US locale.");
            }
            return;
        }

        if (!isBlank(address.postalCode()) && !address.postalCode().matches("^\\d{6}$")) {
            throw new IllegalArgumentException("Postal code must be 6 digits when provided for non en-US locales.");
        }
    }

    private Language resolveLanguage(String lang) {
        String normalized = LocalizationTextUtil.normalizeLanguageCode(lang);
        return languageRepository.findByCodeIgnoreCase(normalized)
                .or(() -> languageRepository.findFirstByDefaultLanguageTrue())
                .orElseThrow(() -> new IllegalArgumentException("No language data available."));
    }

    private Currency resolveCurrency(String code) {
        String normalized = code == null ? "" : code.trim();
        return currencyRepository.findByCodeIgnoreCase(normalized)
                .or(() -> currencyRepository.findFirstByDefaultCurrencyTrue())
                .orElseThrow(() -> new IllegalArgumentException("No currency data available."));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void validateSessionKey(String sessionKey) {
        if (sessionKey == null || sessionKey.isBlank()) {
            throw new IllegalArgumentException("X-Cart-Session header is required.");
        }
    }
}
