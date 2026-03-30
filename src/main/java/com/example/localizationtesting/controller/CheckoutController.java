package com.example.localizationtesting.controller;

import com.example.localizationtesting.dto.CheckoutPreviewRequest;
import com.example.localizationtesting.dto.CheckoutPreviewResponse;
import com.example.localizationtesting.service.CheckoutService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/preview")
    public CheckoutPreviewResponse preview(
            @RequestHeader("X-Cart-Session") String cartSession,
            @RequestBody CheckoutPreviewRequest request,
            @RequestParam(name = "lang", required = false, defaultValue = "en") String lang,
            @RequestParam(name = "currency", required = false, defaultValue = "USD") String currency) {
        return checkoutService.preview(cartSession, request, lang, currency);
    }
}
