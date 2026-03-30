package com.example.localizationtesting.repository;

import com.example.localizationtesting.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByCodeIgnoreCase(String code);

    Optional<Currency> findFirstByDefaultCurrencyTrue();
}