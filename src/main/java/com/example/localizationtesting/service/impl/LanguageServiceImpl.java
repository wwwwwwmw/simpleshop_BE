package com.example.localizationtesting.service.impl;

import com.example.localizationtesting.dto.LanguageRequest;
import com.example.localizationtesting.entity.Language;
import com.example.localizationtesting.exception.ResourceNotFoundException;
import com.example.localizationtesting.repository.LanguageRepository;
import com.example.localizationtesting.service.LanguageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LanguageServiceImpl implements LanguageService {

    private final LanguageRepository languageRepository;

    public LanguageServiceImpl(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    @Override
    public List<Language> getAllLanguages() {
        return languageRepository.findAll();
    }

    @Override
    public Language createLanguage(LanguageRequest request) {
        Language language = new Language();
        language.setCode(request.code());
        language.setName(request.name());
        language.setLocaleCode(request.localeCode());
        language.setRtl(request.rtl());
        language.setActive(request.active());
        language.setDefaultLanguage(request.defaultLanguage());

        return languageRepository.save(language);
    }

    @Override
    public Language updateActive(String code, boolean active) {
        Language language = languageRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with code: " + code));

        if (language.isDefaultLanguage() && !active) {
            throw new IllegalArgumentException("Cannot deactivate the current default language.");
        }

        language.setActive(active);
        return languageRepository.save(language);
    }

    @Override
    public Language setDefault(String code) {
        Language target = languageRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with code: " + code));

        if (!target.isActive()) {
            throw new IllegalArgumentException("Cannot set an inactive language as default.");
        }

        languageRepository.findAll().forEach(language -> {
            boolean shouldBeDefault = language.getId().equals(target.getId());
            if (language.isDefaultLanguage() != shouldBeDefault) {
                language.setDefaultLanguage(shouldBeDefault);
                languageRepository.save(language);
            }
        });

        return languageRepository.findById(target.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with id: " + target.getId()));
    }
}