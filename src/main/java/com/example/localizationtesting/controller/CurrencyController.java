package com.example.localizationtesting.controller;

import com.example.localizationtesting.dto.CurrencyRequest;
import com.example.localizationtesting.dto.CurrencyResponse;
import com.example.localizationtesting.entity.Currency;
import com.example.localizationtesting.service.CurrencyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    public List<CurrencyResponse> getAllCurrencies() {
        return currencyService.getAllCurrencies().stream().map(this::toResponse).toList();
    }

    @PostMapping
    public CurrencyResponse createCurrency(@RequestBody CurrencyRequest request) {
        return toResponse(currencyService.createCurrency(request));
    }

    @PatchMapping("/{code}/active")
    public CurrencyResponse updateActive(@PathVariable("code") String code,
            @RequestBody java.util.Map<String, Boolean> request) {
        boolean active = request != null && Boolean.TRUE.equals(request.get("active"));
        return toResponse(currencyService.updateActive(code, active));
    }

    @PatchMapping("/{code}/default")
    public CurrencyResponse setDefault(@PathVariable("code") String code) {
        return toResponse(currencyService.setDefault(code));
    }

    private CurrencyResponse toResponse(Currency currency) {
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