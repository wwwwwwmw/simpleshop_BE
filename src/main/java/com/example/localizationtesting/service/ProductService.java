package com.example.localizationtesting.service;

import com.example.localizationtesting.dto.ProductRequest;
import com.example.localizationtesting.dto.ProductQueryResult;
import com.example.localizationtesting.dto.ProductResponse;

public interface ProductService {
    ProductQueryResult getAllProducts(String lang, String currency, String keyword, Long categoryId, int page, int size,
            String sort);

    ProductResponse getProductById(Long id, String lang, String currency);

    ProductResponse createProduct(ProductRequest request);
}