package org.example.casestudy.controller;

import org.example.casestudy.entities.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.example.casestudy.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
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
    public List<Order> getByDate(@RequestParam UUID customerId,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime start,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime end) {
        return orderService.listOrders(customerId, start, end);
    }

    @PostMapping("/match/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void matchOrder(@PathVariable UUID id) {
        orderService.matchOrder(id);
    }

    @PostMapping("/delete/{id}/{customerId}")
    public void deleteOrder(@PathVariable UUID id, @PathVariable UUID customerId) {
        orderService.deleteOrder(id, customerId);
    }


}
