package com.example.localizationtesting.repository;

import com.example.localizationtesting.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(String customerEmail);

    Optional<Order> findByIdAndCustomerEmailIgnoreCase(Long id, String customerEmail);
}