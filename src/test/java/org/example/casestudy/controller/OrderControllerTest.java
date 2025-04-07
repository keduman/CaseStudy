package org.example.casestudy.controller;

import org.example.casestudy.entities.Order;
import org.example.casestudy.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_Success() {
        Order order = new Order();

        orderController.createOrder(order);

        verify(orderService, times(1)).createOrder(order);
    }

    @Test
    void getByDate_Success() {
        UUID customerId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<Order> expectedOrders = new ArrayList<>();
        expectedOrders.add(new Order());
        expectedOrders.add(new Order());

        when(orderService.listOrders(customerId, start, end)).thenReturn(expectedOrders);

        List<Order> actualOrders = orderController.getByDate(customerId, start, end);

        assertEquals(expectedOrders.size(), actualOrders.size());
        verify(orderService, times(1)).listOrders(customerId, start, end);
    }

    @Test
    void matchOrder_Success() {
        UUID orderId = UUID.randomUUID();

        orderController.matchOrder(orderId);

        verify(orderService, times(1)).matchOrder(orderId);
    }

    @Test
    void deleteOrder_Success() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        orderController.deleteOrder(orderId, customerId);

        verify(orderService, times(1)).deleteOrder(orderId, customerId);
    }
}