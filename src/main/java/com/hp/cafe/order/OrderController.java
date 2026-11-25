package com.hp.cafe.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDtos.OrderResponse> create(@AuthenticationPrincipal String userId,
                                                           @RequestBody OrderDtos.CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(userId, request));
    }

    @GetMapping
    public List<OrderDtos.OrderResponse> list(@AuthenticationPrincipal String userId) {
        return orderService.list(userId);
    }

    @GetMapping("/store/{storeLocation}")
    public List<OrderDtos.OrderResponse> storeQueue(@PathVariable String storeLocation) {
        return orderService.listStoreQueue(storeLocation);
    }

    @PatchMapping("/{orderId}/status")
    public OrderDtos.OrderResponse updateStatus(@PathVariable String orderId,
                                                @RequestParam String status) {
        return orderService.updateStatus(orderId, status.trim().toUpperCase());
    }
}
