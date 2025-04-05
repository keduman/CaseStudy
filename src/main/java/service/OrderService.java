package service;

import entities.Asset;
import entities.Order;
import enums.OrderStatus;
import enums.OrderType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.AssetRepository;
import repositories.OrderRepository;

import java.util.Arrays;
import java.util.Date;
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


    public void createOrder(Order order) {
        var asset = assetRepository.findByCustomerIdAndAssetName(
                        order.getCustomerId(),
                        order.getOrderType() == OrderType.BUY ? "TRY" : order.getAssetName()
                )
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        var required = order.getSize() * (order.getOrderType() == OrderType.BUY ? order.getPrice() : 1);
        if(asset.getUsableSize() < required) {
            throw new RuntimeException("Insufficient balance");
        }

        asset.setUsableSize(asset.getUsableSize() - required);
        assetRepository.save(asset);

        order.setOrderStatus(OrderStatus.PENDING);
        order.setCreatedDate(new Date());
        orderRepository.save(order);
    }

    public void matchOrder(UUID orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if(order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order is not PENDING");
        }

        var assetName = order.getOrderType() == OrderType.BUY ? order.getAssetName() : "TRY";
        var value = order.getOrderType() == OrderType.BUY ? order.getSize() : order.getSize() * order.getPrice();

        var asset = assetRepository.findByCustomerIdAndAssetName(
                        order.getCustomerId(),
                        assetName
                )
                .orElseGet(() -> Asset.builder()
                        .customerId(order.getCustomerId())
                        .assetName(assetName)
                        .size(0.0)
                        .usableSize(0.0)
                        .build());

        asset.setSize(asset.getSize() + value);
        asset.setUsableSize(asset.getUsableSize() + value);
        assetRepository.save(asset);

        order.setOrderStatus(OrderStatus.MATCHED);
        orderRepository.save(order);

    }

    public List<Order> getOrders(String customerId, Date start, Date end) {
        return orderRepository.findByCustomerIdAndCreatedDateBetween(customerId, start, end);
    }

    public void cancelOrder(UUID orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if(order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be cancelled");
        }

        var asset = assetRepository.findByCustomerIdAndAssetName(
                        order.getCustomerId(),
                        order.getOrderType() == OrderType.BUY ? "TRY" : order.getAssetName()
                )
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        var refund = order.getSize() * (order.getOrderType() == OrderType.BUY ? order.getPrice() : 1);
        asset.setUsableSize(asset.getUsableSize() + refund);
        assetRepository.save(asset);

        order.setOrderStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }
}
