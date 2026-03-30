package com.example.localizationtesting.repository;

import com.example.localizationtesting.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByNameEnIgnoreCase(String nameEn);

    Optional<Product> findByImageUrlIgnoreCase(String imageUrl);
}