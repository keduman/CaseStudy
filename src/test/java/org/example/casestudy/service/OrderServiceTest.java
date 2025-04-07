package org.example.casestudy.service;

import org.example.casestudy.entities.Asset;
import org.example.casestudy.entities.Order;
import org.example.casestudy.enums.OrderStatus;
import org.example.casestudy.enums.OrderType;
import org.example.casestudy.repositories.AssetRepository;
import org.example.casestudy.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private Logger logger;

    @InjectMocks
    private OrderService orderService;

    private UUID customerId;
    private String assetName;
    private OrderType orderType;
    private long size;
    private double price;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        customerId = UUID.randomUUID();
        assetName = "TEST_ASSET";
        orderType = OrderType.BUY;
        size = 10L;
        price = 100.0;
        orderId = UUID.randomUUID();
    }

    @Test
    void createOrder_BuyOrder_Success() {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setAssetName(assetName);
        order.setOrderType(orderType);
        order.setSize(size);
        order.setPrice(price);

        Asset asset = new Asset();
        asset.setCustomerId(customerId);
        asset.setAssetName(assetName);
        asset.setUsableSize(1000L);

        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(customerId);
        tryAsset.setAssetName("TRY");
        tryAsset.setUsableSize(100000L);

        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(asset));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")).thenReturn(Optional.of(tryAsset));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);

        Order createdOrder = orderService.createOrder(order);

        assertNotNull(createdOrder);
        assertEquals(OrderStatus.PENDING, createdOrder.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void createOrder_BuyOrder_InsufficientFunds() {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setAssetName(assetName);
        order.setOrderType(orderType);
        order.setSize(size);
        order.setPrice(price);

        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(customerId);
        tryAsset.setAssetName("TRY");
        tryAsset.setUsableSize((long) (size * price - 1));

        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(new Asset()));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")).thenReturn(Optional.of(tryAsset));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(order));

        verify(assetRepository, never()).save(any(Asset.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_SellOrder_Success() {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setAssetName(assetName);
        order.setOrderType(OrderType.SELL);
        order.setSize(size);
        order.setPrice(price);

        Asset asset = new Asset();
        asset.setCustomerId(customerId);
        asset.setAssetName(assetName);
        asset.setUsableSize(size);

        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(asset));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);

        Order createdOrder = orderService.createOrder(order);

        assertNotNull(createdOrder);
        assertEquals(OrderStatus.PENDING, createdOrder.getStatus());
        verify(assetRepository, times(1)).save(asset);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void createOrder_SellOrder_InsufficientAssets() {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setAssetName(assetName);
        order.setOrderType(OrderType.SELL);
        order.setSize(size);
        order.setPrice(price);

        Asset asset = new Asset();
        asset.setCustomerId(customerId);
        asset.setAssetName(assetName);
        asset.setUsableSize(size - 1);

        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(asset));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(order));

        verify(assetRepository, never()).save(any(Asset.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void listOrders_Success() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<Order> expectedOrders = new ArrayList<>();
        expectedOrders.add(new Order());
        expectedOrders.add(new Order());

        when(orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate)).thenReturn(expectedOrders);

        List<Order> actualOrders = orderService.listOrders(customerId, startDate, endDate);

        assertEquals(expectedOrders.size(), actualOrders.size());
        verify(orderRepository, times(1)).findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
    }

    @Test
    void deleteOrder_PendingOrder_Success() {
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(customerId);
        order.setAssetName(assetName);
        order.setOrderType(orderType);
        order.setSize(size);
        order.setPrice(price);
        order.setStatus(OrderStatus.PENDING);

        Asset asset = new Asset();
        asset.setCustomerId(customerId);
        asset.setAssetName(assetName);
        asset.setUsableSize(0L);

        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(customerId);
        tryAsset.setAssetName("TRY");
        tryAsset.setUsableSize(1000L);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(asset));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")).thenReturn(Optional.of(tryAsset));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);

        orderService.deleteOrder(orderId, customerId);

        assertEquals(OrderStatus.CANCELED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void deleteOrder_NotPendingOrder_Failure() {
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(customerId);
        order.setStatus(OrderStatus.MATCHED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> orderService.deleteOrder(orderId, customerId));

        verify(orderRepository, never()).save(any(Order.class));
        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    void deleteOrder_OrderNotFound_Failure() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.deleteOrder(orderId, customerId));

        verify(orderRepository, never()).save(any(Order.class));
        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    void deleteOrder_OrderDoesNotBelongToCustomer_Failure() {
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> orderService.deleteOrder(orderId, customerId));

        verify(orderRepository, never()).save(any(Order.class));
        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    void listAssets_Success() {
        List<Asset> expectedAssets = new ArrayList<>();
        expectedAssets.add(new Asset());
        expectedAssets.add(new Asset());

        when(assetRepository.findByCustomerId(customerId)).thenReturn(expectedAssets);

        List<Asset> actualAssets = orderService.listAssets(customerId);

        assertEquals(expectedAssets.size(), actualAssets.size());
        verify(assetRepository, times(1)).findByCustomerId(customerId);
    }

    @Test
    void matchOrder_PendingOrder_Success() {
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(customerId);
        order.setAssetName(assetName);
        order.setOrderType(orderType);
        order.setSize(size);
        order.setPrice(price);
        order.setStatus(OrderStatus.PENDING);

        Asset asset = new Asset();
        asset.setCustomerId(customerId);
        asset.setAssetName(assetName);
        asset.setSize(0L);
        asset.setUsableSize(0L);

        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(customerId);
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(100000L);
        tryAsset.setUsableSize(100000L);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(asset));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")).thenReturn(Optional.of(tryAsset));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);

        orderService.matchOrder(orderId);

        assertEquals(OrderStatus.MATCHED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
        verify(assetRepository, times(2)).save(any(Asset.class));
    }

    @Test
    void matchOrder_NotPendingOrder_Failure() {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.MATCHED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> orderService.matchOrder(orderId));

        verify(orderRepository, never()).save(any(Order.class));
        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    void matchOrder_OrderNotFound_Failure() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.matchOrder(orderId));

        verify(orderRepository, never()).save(any(Order.class));
        verify(assetRepository, never()).save(any(Asset.class));
    }

    // ---  Helper Method Tests ---

    @Test
    void findOrCreateAsset_AssetExists() {
        Asset existingAsset = new Asset();
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(existingAsset));

        Asset result = orderService.findOrCreateAsset(customerId, assetName);

        assertEquals(existingAsset, result);
        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    void findOrCreateAsset_AssetDoesNotExist() {
        Asset newAsset = new Asset();
        newAsset.setCustomerId(customerId);
        newAsset.setAssetName(assetName);
        newAsset.setSize(0L);
        newAsset.setUsableSize(0L);

        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.empty());
        when(assetRepository.save(any(Asset.class))).thenReturn(newAsset);

        Asset result = orderService.findOrCreateAsset(customerId, assetName);

        assertEquals(newAsset, result);
        verify(assetRepository, times(1)).save(any(Asset.class));
    }

    @Test
    void createNewAsset_Success() {
        Asset newAsset = new Asset();
        newAsset.setCustomerId(customerId);
        newAsset.setAssetName(assetName);
        newAsset.setSize(0L);
        newAsset.setUsableSize(0L);

        when(assetRepository.save(any(Asset.class))).thenReturn(newAsset);

        Asset result = orderService.createNewAsset(customerId, assetName);

        assertEquals(newAsset, result);
        verify(assetRepository, times(1)).save(any(Asset.class));
    }

    @Test
    void findOrderByIdAndCustomerId_OrderExistsAndBelongsToCustomer() {
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(customerId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order result = orderService.findOrderByIdAndCustomerId(orderId, customerId);

        assertEquals(order, result);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void findOrderByIdAndCustomerId_OrderExistsButDoesNotBelongToCustomer() {
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(UUID.randomUUID()); // Different customer ID

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> orderService.findOrderByIdAndCustomerId(orderId, customerId));

        verify(orderRepository, times(1)).findById(orderId);
    }
}