package com.example.localizationtesting.service;

import com.example.localizationtesting.dto.CheckoutPreviewRequest;
import com.example.localizationtesting.dto.CheckoutPreviewResponse;

public interface CheckoutService {
    CheckoutPreviewResponse preview(String cartSession, CheckoutPreviewRequest request, String lang, String currency);
}
