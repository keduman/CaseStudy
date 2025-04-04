package repositories;

import entities.Order;
import enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByCustomerIdAndCreatedDateBetween(String customerId, Date from, Date to);
    List<Order> findByOrderStatus(OrderStatus orderStatus);
}
