package org.example.casestudy.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.casestudy.entities.Asset;
import org.example.casestudy.entities.Order;
import org.example.casestudy.enums.OrderStatus;
import org.example.casestudy.enums.OrderType;
import org.example.casestudy.repositories.AssetRepository;
import org.example.casestudy.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, AssetRepository assetRepository) {
        this.orderRepository = orderRepository;
        this.assetRepository = assetRepository;
    }


    @Transactional
    public Order createOrder(Order order) {
        Asset asset = findOrCreateAsset(order.getCustomerId(), order.getAssetName());

        if (order.getOrderType() == OrderType.BUY) {
            updateTRYBalanceForBuy(order.getCustomerId(), order.getSize(), order.getPrice());
        } else if (order.getOrderType() == OrderType.SELL) {
            updateAssetBalanceForSell(asset, order.getSize());
        }

        order.setStatus(OrderStatus.PENDING);
        return orderRepository.save(order);
    }

    public List<Order> listOrders(UUID customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
    }

    @Transactional
    public void deleteOrder(UUID orderId, UUID customerId) {
        Order order = findOrderByIdAndCustomerId(orderId, customerId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING orders can be canceled");
        }

        updateBalancesOnOrderCancellation(order);

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    public List<Asset> listAssets(UUID customerId) {
        return assetRepository.findByCustomerId(customerId);
    }


    @Transactional
    public void matchOrder(UUID orderId) {
        Order order = findPendingOrderById(orderId);
        updateBalancesOnOrderMatch(order);
        order.setStatus(OrderStatus.MATCHED);
        orderRepository.save(order);
    }


    private Asset findOrCreateAsset(UUID customerId, String assetName) {
        return assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                .orElseGet(() -> createNewAsset(customerId, assetName));
    }

    private Asset createNewAsset(UUID customerId, String assetName) {
        Asset newAsset = new Asset();
        newAsset.setCustomerId(customerId);
        newAsset.setAssetName(assetName);
        newAsset.setSize(0L);
        newAsset.setUsableSize(0L);
        return assetRepository.save(newAsset);
    }

    private Order findOrderByIdAndCustomerId(UUID orderId, UUID customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!order.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Order does not belong to customer");
        }
        return order;
    }

    private Order findPendingOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING orders can be matched");
        }
        return order;
    }

    private void updateTRYBalanceForBuy(UUID customerId, long size, double price) {
        Asset tryAsset = findOrCreateAsset(customerId, "TRY");
        long cost = (long) (size * price);
        if (tryAsset.getUsableSize() < cost) {
            throw new IllegalArgumentException("Insufficient TRY balance");
        }
        tryAsset.setUsableSize(tryAsset.getUsableSize() - cost);
        assetRepository.save(tryAsset);
    }

    private void updateAssetBalanceForSell(Asset asset, long size) {
        if (asset.getUsableSize() < size) {
            throw new IllegalArgumentException("Insufficient asset balance");
        }
        asset.setUsableSize(asset.getUsableSize() - size);
        assetRepository.save(asset);
    }

    private void updateBalancesOnOrderCancellation(Order order) {
        UUID customerId = order.getCustomerId();
        String assetName = order.getAssetName();
        OrderType orderType = order.getOrderType();
        long size = order.getSize();
        double price = order.getPrice();

        Asset asset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));

        if (orderType == OrderType.BUY) {
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")
                    .orElseThrow(() -> new IllegalArgumentException("TRY asset not found"));
            long cost = (long) (size * price);
            tryAsset.setUsableSize(tryAsset.getUsableSize() + cost);
            assetRepository.save(tryAsset);
        } else if (orderType == OrderType.SELL) {
            asset.setUsableSize(asset.getUsableSize() + size);
            assetRepository.save(asset);
        }
    }

    private void updateBalancesOnOrderMatch(Order order) {
        UUID customerId = order.getCustomerId();
        String assetName = order.getAssetName();
        OrderType orderType = order.getOrderType();
        long size = order.getSize();
        double price = order.getPrice();

        Asset asset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found"));
        Asset tryAsset = findOrCreateAsset(customerId, "TRY");

        long cost = (long) (size * price);

        if (orderType == OrderType.BUY) {
            tryAsset.setSize(tryAsset.getSize() - cost);
            asset.setSize(asset.getSize() + size);
            asset.setUsableSize(asset.getUsableSize() + size);
        } else if (orderType == OrderType.SELL) {
            tryAsset.setSize(tryAsset.getSize() + cost);
            tryAsset.setUsableSize(tryAsset.getUsableSize() + cost);
            asset.setSize(asset.getSize() - size);
        }

        assetRepository.save(asset);
        assetRepository.save(tryAsset);
    }
}