package com.example.localizationtesting.service;

import com.example.localizationtesting.dto.CurrencyRequest;
import com.example.localizationtesting.entity.Currency;

import java.util.List;

public interface CurrencyService {
    List<Currency> getAllCurrencies();

    Currency createCurrency(CurrencyRequest request);

    Currency updateActive(String code, boolean active);

    Currency setDefault(String code);
}