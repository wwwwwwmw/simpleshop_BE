package com.example.localizationtesting.controller;

import com.example.localizationtesting.dto.LanguageRequest;
import com.example.localizationtesting.dto.LanguageResponse;
import com.example.localizationtesting.entity.Language;
import com.example.localizationtesting.service.LanguageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/languages")
public class LanguageController {

    private final LanguageService languageService;

    public LanguageController(LanguageService languageService) {
        this.languageService = languageService;
    }

    @GetMapping
    public List<LanguageResponse> getAllLanguages() {
        return languageService.getAllLanguages().stream().map(this::toResponse).toList();
    }

    @PostMapping
    public LanguageResponse createLanguage(@RequestBody LanguageRequest request) {
        return toResponse(languageService.createLanguage(request));
    }

    @PatchMapping("/{code}/active")
    public LanguageResponse updateActive(@PathVariable("code") String code,
            @RequestBody java.util.Map<String, Boolean> request) {
        boolean active = request != null && Boolean.TRUE.equals(request.get("active"));
        return toResponse(languageService.updateActive(code, active));
    }

    @PatchMapping("/{code}/default")
    public LanguageResponse setDefault(@PathVariable("code") String code) {
        return toResponse(languageService.setDefault(code));
    }

    private LanguageResponse toResponse(Language language) {
        return new LanguageResponse(
                language.getId(),
                language.getCode(),
                language.getName(),
                language.getLocaleCode(),
                language.getLocaleCode(),
                language.isRtl(),
                language.isRtl(),
                language.isActive(),
                language.isDefaultLanguage(),
                language.isDefaultLanguage());
    }
}