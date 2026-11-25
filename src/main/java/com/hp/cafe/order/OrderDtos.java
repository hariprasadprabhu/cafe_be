package com.hp.cafe.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderDtos {
    public record CreateOrderRequest(String paymentMethod, List<OrderItemRequest> items) {}
    public record OrderItemRequest(Long productId, String name, Integer quantity, BigDecimal unitPrice) {}
    public record OrderItemResponse(Long productId, String name, Integer quantity, BigDecimal unitPrice) {}
    public record OrderResponse(String id, String status, String paymentMethod, BigDecimal totalAmount,
                                Instant createdAt, String storeLocation, String customerName,
                                List<OrderItemResponse> items) {}
}
