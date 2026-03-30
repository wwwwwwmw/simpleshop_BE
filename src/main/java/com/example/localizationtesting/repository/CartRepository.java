package com.example.localizationtesting.repository;

import com.example.localizationtesting.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findBySessionKeyIgnoreCase(String sessionKey);
}