package com.example.localizationtesting.service;

import com.example.localizationtesting.dto.LanguageRequest;
import com.example.localizationtesting.entity.Language;

import java.util.List;

public interface LanguageService {
    List<Language> getAllLanguages();

    Language createLanguage(LanguageRequest request);

    Language updateActive(String code, boolean active);

    Language setDefault(String code);
}