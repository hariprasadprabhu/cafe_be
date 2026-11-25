package com.hp.cafe.order;

import java.time.Instant;

public record OrderStatusEvent(
    String orderId,
    String userId,
    String status,
    String storeLocation,
    Instant updatedAt
) {}
