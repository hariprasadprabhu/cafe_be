package com.hp.cafe.order;

import com.hp.cafe.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findByStoreLocationOrderByCreatedAtAsc(String storeLocation);
}
