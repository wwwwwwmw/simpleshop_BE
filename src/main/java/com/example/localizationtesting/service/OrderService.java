package com.example.localizationtesting.service;

import com.example.localizationtesting.dto.OrderRequest;
import com.example.localizationtesting.dto.OrderResponse;

import java.util.List;

public interface OrderService {
    List<OrderResponse> getAllOrders();

    OrderResponse getOrderByIdForAdmin(Long id);

    List<OrderResponse> getOrdersForCustomer(String customerEmail);

    OrderResponse getOrderForCustomer(Long id, String customerEmail);

    OrderResponse createOrder(OrderRequest request, String customerEmail, String cartSession);
}