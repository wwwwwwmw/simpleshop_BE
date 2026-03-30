package com.example.localizationtesting.controller;

import com.example.localizationtesting.dto.OrderRequest;
import com.example.localizationtesting.dto.OrderResponse;
import com.example.localizationtesting.service.AuthService;
import com.example.localizationtesting.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final AuthService authService;

    public OrderController(OrderService orderService, AuthService authService) {
        this.orderService = orderService;
        this.authService = authService;
    }

    @GetMapping({ "", "/me" })
    public List<OrderResponse> getAllOrders(@RequestHeader("Authorization") String authorizationHeader) {
        String customerEmail = authService.me(authorizationHeader).email();
        return orderService.getOrdersForCustomer(customerEmail);
    }

    @GetMapping({ "/{id}", "/me/{id}" })
    public OrderResponse getOrderById(@RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("id") Long id) {
        String customerEmail = authService.me(authorizationHeader).email();
        return orderService.getOrderForCustomer(id, customerEmail);
    }

    @PostMapping
    public OrderResponse createOrder(@RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader(name = "X-Cart-Session", required = false) String cartSession,
            @RequestBody OrderRequest request) {
        String customerEmail = authService.me(authorizationHeader).email();
        return orderService.createOrder(request, customerEmail, cartSession);
    }
}