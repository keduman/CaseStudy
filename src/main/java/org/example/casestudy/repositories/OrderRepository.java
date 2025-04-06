package org.example.casestudy.repositories;

import org.example.casestudy.entities.Order;
import org.example.casestudy.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByCustomerIdAndCreateDateBetween(UUID customerId, LocalDateTime from, LocalDateTime to);
    List<Order> findByCustomerId(UUID customerId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByCustomerIdAndStatus(UUID customerId, OrderStatus status);
}
