package com.example.localizationtesting.service.impl;

import com.example.localizationtesting.dto.OrderRequest;
import com.example.localizationtesting.dto.OrderResponse;
import com.example.localizationtesting.entity.Cart;
import com.example.localizationtesting.entity.CartItem;
import com.example.localizationtesting.entity.Currency;
import com.example.localizationtesting.entity.Order;
import com.example.localizationtesting.entity.OrderItem;
import com.example.localizationtesting.entity.Product;
import com.example.localizationtesting.exception.ResourceNotFoundException;
import com.example.localizationtesting.repository.CartRepository;
import com.example.localizationtesting.repository.CurrencyRepository;
import com.example.localizationtesting.repository.OrderRepository;
import com.example.localizationtesting.repository.ProductRepository;
import com.example.localizationtesting.service.OrderService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CurrencyRepository currencyRepository;

    public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository,
            CartRepository cartRepository, CurrencyRepository currencyRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.currencyRepository = currencyRepository;
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public OrderResponse getOrderByIdForAdmin(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        return toResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersForCustomer(String customerEmail) {
        return orderRepository.findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(customerEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public OrderResponse getOrderForCustomer(Long id, String customerEmail) {
        Order order = orderRepository.findByIdAndCustomerEmailIgnoreCase(id, customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        return toResponse(order);
    }

    @Override
    public OrderResponse createOrder(OrderRequest request, String customerEmail, String cartSession) {
        Order order = new Order();
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(customerEmail);
        order.setCustomerPhone(request.customerPhone());
        order.setShippingAddressLine(request.resolvedAddressLine());
        order.setShippingCity(request.resolvedCity());
        order.setShippingState(request.resolvedState());
        order.setShippingPostalCode(request.resolvedPostalCode());
        order.setCustomerNote(request.customerNote());
        order.setCurrencyCode(request.resolvedCurrencyCode());
        order.setPaymentMethod(request.resolvedPaymentMethod());
        order.setDiscountBase(BigDecimal.ZERO);
        order.setStatus("NEW");
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> mappedItems = mapItemsFromCart(cartSession, order);
        if (mappedItems.isEmpty()) {
            mappedItems = mapItemsFromRequest(request, order);
        }
        if (!mappedItems.isEmpty()) {
            order.setItems(mappedItems);
        }

        Order saved = orderRepository.save(order);
        clearCartItems(cartSession);

        return toResponse(saved);
    }

    private List<OrderItem> mapItemsFromCart(String cartSession, Order order) {
        if (cartSession == null || cartSession.isBlank()) {
            return List.of();
        }

        Cart cart = cartRepository.findBySessionKeyIgnoreCase(cartSession.trim()).orElse(null);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return List.of();
        }

        Currency selectedCurrency = resolveCurrency(order.getCurrencyCode());
        int decimalDigits = Math.max(selectedCurrency.getDecimalDigits(), 0);

        List<OrderItem> mappedItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            if (cartItem == null || cartItem.getProduct() == null) {
                continue;
            }

            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity() == null || cartItem.getQuantity() <= 0
                    ? 1
                    : cartItem.getQuantity();

            BigDecimal baseUnitPrice = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
            BigDecimal unitPrice = baseUnitPrice
                    .multiply(selectedCurrency.getExchangeRateToDefault())
                    .setScale(decimalDigits, RoundingMode.HALF_UP);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);
            mappedItems.add(orderItem);
        }

        return mappedItems;
    }

    private List<OrderItem> mapItemsFromRequest(OrderRequest request, Order order) {
        List<OrderItem> mappedItems = new ArrayList<>();
        if (request == null || request.items() == null || request.items().isEmpty()) {
            return mappedItems;
        }

        for (OrderRequest.OrderItemRequest requestItem : request.items()) {
            if (requestItem == null || requestItem.productId() == null) {
                continue;
            }

            Product product = productRepository.findById(requestItem.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + requestItem.productId()));

            int quantity = requestItem.quantity() == null || requestItem.quantity() <= 0
                    ? 1
                    : requestItem.quantity();

            BigDecimal unitPrice = requestItem.unitPrice() != null
                    ? requestItem.unitPrice()
                    : (product.getPrice() == null ? BigDecimal.ZERO : product.getPrice());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);
            mappedItems.add(orderItem);
        }

        return mappedItems;
    }

    private void clearCartItems(String cartSession) {
        if (cartSession == null || cartSession.isBlank()) {
            return;
        }

        cartRepository.findBySessionKeyIgnoreCase(cartSession.trim()).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems() == null
                ? List.of()
                : order.getItems().stream().map(item -> {
                    int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
                    BigDecimal unitPrice = item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice();
                    BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                    String productName = item.getProduct() != null ? item.getProduct().getNameEn() : "Unknown product";
                    Long productId = item.getProduct() != null ? item.getProduct().getId() : null;

                    return new OrderResponse.OrderItemResponse(
                            productId,
                            productName,
                            quantity,
                            unitPrice,
                            lineTotal);
                }).toList();

        BigDecimal subtotal = itemResponses.stream()
                .map(OrderResponse.OrderItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal amountPayable = subtotal;

        return new OrderResponse(
                order.getId(),
                order.getId(),
                toOrderCode(order.getId()),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getCustomerPhone(),
                normalizePaymentMethod(order.getPaymentMethod()),
                order.getDiscountBase(),
                new OrderResponse.ShippingAddress(
                        order.getShippingAddressLine(),
                        order.getShippingCity(),
                        order.getShippingState(),
                        order.getShippingPostalCode()),
                order.getShippingAddressLine(),
                order.getShippingCity(),
                order.getShippingState(),
                order.getShippingPostalCode(),
                order.getCustomerNote(),
                order.getStatus(),
                order.getCreatedAt(),
                itemResponses,
                subtotal,
                discount,
                normalizeCurrencyCode(order.getCurrencyCode()),
                amountPayable);
    }

    private String toOrderCode(Long id) {
        if (id == null) {
            return "ORD-PENDING";
        }
        return "ORD-" + String.format("%06d", id);
    }

    private String normalizePaymentMethod(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "COD";
        }

        if ("NEW".equalsIgnoreCase(rawStatus) || "PROCESSING".equalsIgnoreCase(rawStatus)
                || "DONE".equalsIgnoreCase(rawStatus) || "CANCELLED".equalsIgnoreCase(rawStatus)) {
            return "COD";
        }

        return rawStatus;
    }

    private String normalizeCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return "USD";
        }

        return currencyCode.toUpperCase();
    }

    private Currency resolveCurrency(String code) {
        String normalized = code == null ? "" : code.trim();
        return currencyRepository.findByCodeIgnoreCase(normalized)
                .or(() -> currencyRepository.findFirstByDefaultCurrencyTrue())
                .orElseThrow(() -> new IllegalArgumentException("No currency data available."));
    }
}