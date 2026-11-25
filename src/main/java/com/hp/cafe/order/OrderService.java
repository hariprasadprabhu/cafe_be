package com.hp.cafe.order;

import com.hp.cafe.auth.User;
import com.hp.cafe.auth.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final UserRepository users;
    private final KafkaTemplate<String, String> kafka;
    private final String storeTopic;

    public OrderService(OrderRepository orders, UserRepository users, KafkaTemplate<String, String> kafka,
                        @Value("${app.kafka.store-topic:cafe.orders.manipal}") String storeTopic) {
        this.orders = orders;
        this.users = users;
        this.kafka = kafka;
        this.storeTopic = storeTopic;
    }

    @Transactional
    public OrderDtos.OrderResponse create(String userId, OrderDtos.CreateOrderRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }
        if (request.paymentMethod() == null || request.paymentMethod().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment method is required");
        }
        User user = users.findById(UUID.fromString(userId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token"));

        BigDecimal total = BigDecimal.ZERO;
        Order order = new Order(user, request.paymentMethod().trim(), BigDecimal.ZERO);
        for (var itemRequest : request.items()) {
            if (itemRequest.quantity() == null || itemRequest.quantity() < 1 || itemRequest.unitPrice() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cart item");
            }
            BigDecimal lineTotal = itemRequest.unitPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            total = total.add(lineTotal);
            order.addItem(new OrderItem(itemRequest.productId(), itemRequest.name(), itemRequest.quantity(), itemRequest.unitPrice()));
        }
        order.setTotalAmount(total);
        order = orders.save(order);

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDtos.OrderResponse> list(String userId) {
        User user = users.findById(UUID.fromString(userId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token"));
        return orders.findByUserOrderByCreatedAtDesc(user).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderDtos.OrderResponse> listStoreQueue(String storeLocation) {
        String location = storeLocation == null || storeLocation.isBlank() ? "Manipal" : storeLocation.trim();
        return orders.findByStoreLocationOrderByCreatedAtAsc(location).stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderDtos.OrderResponse updateStatus(String orderId, String status) {
        if (!status.equals("READY") && !status.equals("SERVED")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be READY or SERVED");
        }
        Order order = orders.findById(UUID.fromString(orderId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        order.setStatus(status);
        order = orders.save(order);

        String eventPayload = """
            {
              "orderId": "%s",
              "userId": "%s",
              "status": "%s",
              "storeLocation": "%s",
              "timestamp": "%s"
            }
            """.formatted(
            order.getId().toString(),
            order.getUser().getId().toString(),
            order.getStatus(),
            order.getStoreLocation(),
            Instant.now()
        );

        String kafkaKey = order.getId().toString();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    kafka.send(storeTopic, kafkaKey, eventPayload);
                } catch (Exception exception) {
                    throw new IllegalStateException("Unable to publish order status event", exception);
                }
            }
        });

        return toResponse(order);
    }

    private OrderDtos.OrderResponse toResponse(Order order) {
        var items = order.getItems().stream()
            .map(item -> new OrderDtos.OrderItemResponse(item.getProductId(), item.getName(), item.getQuantity(), item.getUnitPrice()))
            .toList();
        return new OrderDtos.OrderResponse(order.getId().toString(), order.getStatus(), order.getPaymentMethod(),
            order.getTotalAmount(), order.getCreatedAt(), order.getStoreLocation(),
            order.getUser().getName(), items);
    }
}
