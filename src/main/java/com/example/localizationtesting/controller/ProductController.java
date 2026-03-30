package com.example.localizationtesting.controller;

import com.example.localizationtesting.dto.ProductRequest;
import com.example.localizationtesting.dto.ProductQueryResult;
import com.example.localizationtesting.dto.ProductResponse;
import com.example.localizationtesting.service.ProductService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(name = "lang", required = false, defaultValue = "en") String lang,
            @RequestParam(name = "currency", required = false, defaultValue = "USD") String currency,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size,
            @RequestParam(name = "sort", required = false, defaultValue = "newest") String sort) {
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

    @GetMapping("/{id}")
    public ProductResponse getProductById(
            @PathVariable("id") Long id,
            @RequestParam(name = "lang", required = false, defaultValue = "en") String lang,
            @RequestParam(name = "currency", required = false, defaultValue = "USD") String currency) {
        return productService.getProductById(id, lang, currency);
    }

    @PostMapping
    public ProductResponse createProduct(@RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }
}