package com.example.localizationtesting.repository;

import com.example.localizationtesting.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LanguageRepository extends JpaRepository<Language, Long> {
    Optional<Language> findByCodeIgnoreCase(String code);

    Optional<Language> findFirstByDefaultLanguageTrue();
}