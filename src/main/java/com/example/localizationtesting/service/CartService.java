package com.example.localizationtesting.service;

import com.example.localizationtesting.dto.CartAddItemRequest;
import com.example.localizationtesting.dto.CartResponse;
import com.example.localizationtesting.dto.CartUpdateItemRequest;

public interface CartService {
    CartResponse getCart(String sessionKey, String lang, String currency);

    CartResponse addItem(String sessionKey, String lang, String currency, CartAddItemRequest request);

    CartResponse updateItem(String sessionKey, String lang, String currency, Long itemId,
            CartUpdateItemRequest request);

    CartResponse removeItem(String sessionKey, String lang, String currency, Long itemId);

    void clear(String sessionKey);
}