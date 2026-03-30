package com.example.localizationtesting.service.impl;

import com.example.localizationtesting.dto.CurrencyRequest;
import com.example.localizationtesting.entity.Currency;
import com.example.localizationtesting.exception.ResourceNotFoundException;
import com.example.localizationtesting.repository.CurrencyRepository;
import com.example.localizationtesting.service.CurrencyService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    public CurrencyServiceImpl(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Override
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    @Override
    public Currency createCurrency(CurrencyRequest request) {
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

        return currencyRepository.save(currency);
    }

    @Override
    public Currency updateActive(String code, boolean active) {
        Currency currency = currencyRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with code: " + code));

        if (currency.isDefaultCurrency() && !active) {
            throw new IllegalArgumentException("Cannot deactivate the current default currency.");
        }

        currency.setActive(active);
        return currencyRepository.save(currency);
    }

    @Override
    public Currency setDefault(String code) {
        Currency target = currencyRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with code: " + code));

        if (!target.isActive()) {
            throw new IllegalArgumentException("Cannot set an inactive currency as default.");
        }

        currencyRepository.findAll().forEach(currency -> {
            boolean shouldBeDefault = currency.getId().equals(target.getId());
            if (currency.isDefaultCurrency() != shouldBeDefault) {
                currency.setDefaultCurrency(shouldBeDefault);
                currencyRepository.save(currency);
            }
        });

        return currencyRepository.findById(target.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + target.getId()));
    }
}