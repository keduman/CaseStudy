package controller;

import entities.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import service.OrderService;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public void createOrder(@RequestBody Order order) {
        orderService.createOrder(order);
    }

    @GetMapping
    public List<Order> getByDate(@RequestParam String customerId,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date start,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date end) {
        return orderService.getOrders(customerId, start, end);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/match/{id}")
    public void matchOrder(@PathVariable UUID id) {
        orderService.matchOrder(id);
    }


}
