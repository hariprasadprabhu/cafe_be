package com.hp.cafe.order;

import com.hp.cafe.auth.User;
import com.hp.cafe.auth.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Component
public class OrderStatusKafkaListener {
    private static final Logger log = LoggerFactory.getLogger(OrderStatusKafkaListener.class);
    private final UserRepository users;

    public OrderStatusKafkaListener(UserRepository users) {
        this.users = users;
    }

    @KafkaListener(topics = "${app.kafka.store-topic:cafe.orders.manipal}", groupId = "${spring.kafka.consumer.group-id:cafe-order-status}")
    public void onOrderStatus(String message) {
        try {
            String status = jsonField(message, "status");
            if (!"READY".equals(status)) return;

            String orderId = jsonField(message, "orderId");
            String userId = jsonField(message, "userId");
            if (orderId == null || userId == null) return;

            User user = users.findById(UUID.fromString(userId)).orElse(null);
            if (user == null || user.getPushToken() == null || user.getPushToken().isBlank()) return;

            String json = "{\"to\":\"" + escapeJson(user.getPushToken())
                + "\",\"title\":\"Order ready!\",\"body\":\"Order #" + escapeJson(orderId)
                + " is ready for collection.\",\"sound\":\"default\",\"data\":{\"orderId\":\""
                + escapeJson(orderId) + "\",\"status\":\"READY\"}}";

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://exp.host/--/api/v2/push/send"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception exception) {
            log.error("Failed to deliver Kafka order status event: {}", exception.getMessage());
        }
    }

    private String jsonField(String json, String field) {
        String marker = "\"" + field + "\"";
        int markerIndex = json.indexOf(marker);
        if (markerIndex < 0) return null;
        int colonIndex = json.indexOf(':', markerIndex + marker.length());
        int quoteIndex = json.indexOf('"', colonIndex + 1);
        int quoteEndIndex = json.indexOf('"', quoteIndex + 1);
        if (quoteIndex < 0 || quoteEndIndex < 0) return null;
        return json.substring(quoteIndex + 1, quoteEndIndex);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
