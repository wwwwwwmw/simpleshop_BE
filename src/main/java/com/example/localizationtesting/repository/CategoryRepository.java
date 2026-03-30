package com.example.localizationtesting.repository;

import com.example.localizationtesting.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameEnIgnoreCase(String nameEn);
}