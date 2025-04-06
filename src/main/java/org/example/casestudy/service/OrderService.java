package org.example.casestudy.service;

import org.example.casestudy.entities.Asset;
import org.example.casestudy.entities.Order;
import org.example.casestudy.enums.OrderStatus;
import org.example.casestudy.enums.OrderType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.casestudy.repositories.AssetRepository;
import org.example.casestudy.repositories.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, AssetRepository assetRepository) {
        this.orderRepository = orderRepository;
        this.assetRepository = assetRepository;
    }


    @Transactional
    public Order createOrder(Order order) {
        // Business logic for order creation
        Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())
                .orElseGet(() -> createNewAsset(order.getCustomerId(), order.getAssetName()));

        if (order.getOrderType() == OrderType.BUY) {
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), "TRY")
                    .orElseGet(() -> createNewAsset(order.getCustomerId(), "TRY"));

            if (tryAsset.getUsableSize() < order.getSize() * order.getPrice()) {
                throw new IllegalArgumentException("Insufficient TRY balance");
            }
            tryAsset.setUsableSize(tryAsset.getUsableSize() - (long) (order.getSize() * order.getPrice()));
            assetRepository.save(tryAsset);

        } else if (order.getOrderType() == OrderType.SELL) {
            if (asset.getUsableSize() < order.getSize()) {
                throw new IllegalArgumentException("Insufficient asset balance");
            }
            asset.setUsableSize(asset.getUsableSize() - order.getSize());
            assetRepository.save(asset);
        }

        order.setStatus(OrderStatus.PENDING);

        return orderRepository.save(order);
    }

    public List<Order> listOrders(UUID customerId, LocalDateTime startDate, LocalDateTime endDate) {
        // Business logic for listing orders
        return orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
    }

    @Transactional
    public void deleteOrder(UUID orderId, UUID customerId) {
        // Business logic for deleting an order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Order does not belong to customer");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING orders can be canceled");
        }

        Asset asset = assetRepository.findByCustomerIdAndAssetName(customerId, order.getAssetName())
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));

        if (order.getOrderType() == OrderType.BUY) {
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")
                    .orElseThrow(() -> new IllegalArgumentException("TRY asset not found"));
            tryAsset.setUsableSize(tryAsset.getUsableSize() + (long) (order.getSize() * order.getPrice()));
            assetRepository.save(tryAsset);
        } else if (order.getOrderType() == OrderType.SELL) {
            asset.setUsableSize(asset.getUsableSize() + order.getSize());
            assetRepository.save(asset);
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    public List<Asset> listAssets(UUID customerId) {
        // Business logic for listing assets
        return assetRepository.findByCustomerId(customerId);
    }

    private Asset createNewAsset(UUID customerId, String assetName) {
        Asset newAsset = new Asset();
        newAsset.setCustomerId(customerId);
        newAsset.setAssetName(assetName);
        newAsset.setSize(0L);
        newAsset.setUsableSize(0L);
        return assetRepository.save(newAsset);
    }

    // Bonus 2: Admin order matching
    @Transactional
    public void matchOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING orders can be matched");
        }

        Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));

        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), "TRY")
                .orElseGet(() -> createNewAsset(order.getCustomerId(), "TRY"));

        if (order.getOrderType() == OrderType.BUY) {
            tryAsset.setSize(tryAsset.getSize() - (long) (order.getSize() * order.getPrice()));
            asset.setSize(asset.getSize() + order.getSize());
        } else if (order.getOrderType() == OrderType.SELL) {
            tryAsset.setSize(tryAsset.getSize() + (long) (order.getSize() * order.getPrice()));
            asset.setSize(asset.getSize() - order.getSize());
        }

        assetRepository.save(asset);
        assetRepository.save(tryAsset);

        order.setStatus(OrderStatus.MATCHED);
        orderRepository.save(order);
    }
}
