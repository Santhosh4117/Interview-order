package com.example.demo.service;

import com.example.demo.DTO.OrderRequest;
import com.example.demo.DTO.OrderResponse;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderStatus;
import com.example.demo.exception.OrderProcessingException;
import com.example.demo.exception.OrderValidationException;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository){
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest){
        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new OrderValidationException("Order must have at least one item.");
        }
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderItem> items = new ArrayList<>();

        for(OrderRequest.OrderItemRequest itemRequest: orderRequest.getItems()){
            if (itemRequest.getQuantity() <= 0) {
                throw new OrderValidationException("Quantity must be positive.");
            }
            if (itemRequest.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new OrderValidationException("Unit price must be positive.");
            }
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductCode(itemRequest.getProductCode());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());

            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            items.add(item);
        }
        order.setItems(items);
        order.setTotalAmount(totalAmount);

        try {
            orderRepository.save(order);
        } catch (Exception ex) {
            throw new OrderProcessingException("Failed to save order: " + ex.getMessage());
        }

        return mapToResponse(order);

    }

    private OrderResponse mapToResponse(Order order) {

        OrderResponse orderResponse = new OrderResponse();

        orderResponse.setOrderNumber(order.getOrderNumber());
        orderResponse.setStatus(order.getStatus());
        orderResponse.setTotalAmount(order.getTotalAmount());
        orderResponse.setCreatedAt(order.getCreatedAt());
        // Map OrderItems to OrderItemResponse DTOs
        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> {
                    OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse();
                    itemResponse.setProductCode(item.getProductCode());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setUnitPrice(item.getUnitPrice());
                    return itemResponse;
                })
                .collect(Collectors.toList());

        orderResponse.setItems(itemResponses);
        return orderResponse;

    }
}
