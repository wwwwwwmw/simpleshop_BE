package com.example.localizationtesting.controller;

import com.example.localizationtesting.dto.CartAddItemRequest;
import com.example.localizationtesting.dto.CartResponse;
import com.example.localizationtesting.dto.CartUpdateItemRequest;
import com.example.localizationtesting.service.CartService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse getCart(
            @RequestHeader("X-Cart-Session") String cartSession,
            @RequestParam(name = "lang", required = false, defaultValue = "en") String lang,
            @RequestParam(name = "currency", required = false, defaultValue = "USD") String currency) {
        return cartService.getCart(cartSession, lang, currency);
    }

    @PostMapping("/items")
    public CartResponse addItem(
            @RequestHeader("X-Cart-Session") String cartSession,
            @RequestParam(name = "lang", required = false, defaultValue = "en") String lang,
            @RequestParam(name = "currency", required = false, defaultValue = "USD") String currency,
            @RequestBody CartAddItemRequest request) {
        return cartService.addItem(cartSession, lang, currency, request);
    }

    @PutMapping("/items/{itemId}")
    public CartResponse updateItem(
            @RequestHeader("X-Cart-Session") String cartSession,
            @RequestParam(name = "lang", required = false, defaultValue = "en") String lang,
            @RequestParam(name = "currency", required = false, defaultValue = "USD") String currency,
            @PathVariable("itemId") Long itemId,
            @RequestBody CartUpdateItemRequest request) {
        return cartService.updateItem(cartSession, lang, currency, itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(
            @RequestHeader("X-Cart-Session") String cartSession,
            @RequestParam(name = "lang", required = false, defaultValue = "en") String lang,
            @RequestParam(name = "currency", required = false, defaultValue = "USD") String currency,
            @PathVariable("itemId") Long itemId) {
        return cartService.removeItem(cartSession, lang, currency, itemId);
    }

    @DeleteMapping
    public void clearCart(@RequestHeader("X-Cart-Session") String cartSession) {
        cartService.clear(cartSession);
    }
}