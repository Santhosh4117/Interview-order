package com.example.demo.service;

import com.example.demo.DTO.OrderRequest;
import com.example.demo.DTO.OrderResponse;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderStatus;
import com.example.demo.exception.OrderValidationException;
import com.example.demo.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    public OrderServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createOrder_ShouldCreateOrderSuccessfully() {
        // Prepare test data
        OrderRequest request = new OrderRequest();
        OrderRequest.OrderItemRequest itemRequest = new OrderRequest.OrderItemRequest();
        itemRequest.setProductCode("P001");
        itemRequest.setQuantity(2);
        itemRequest.setUnitPrice(new BigDecimal("10.00"));

        request.setItems(Collections.singletonList(itemRequest));

        // Mock repository behavior
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute the service method
        OrderResponse response = orderService.createOrder(request);

        // Assertions
        assertNotNull(response);
        assertEquals(OrderStatus.CREATED, response.getStatus());
        assertEquals(new BigDecimal("20.00"), response.getTotalAmount());
    }

    @Test
    public void createOrder_ShouldThrowValidationException_WhenNoItems() {
        // Prepare test data
        OrderRequest request = new OrderRequest();
        request.setItems(Collections.emptyList());

        // Execute and assert
        OrderValidationException exception = assertThrows(OrderValidationException.class,
                () -> orderService.createOrder(request));
        assertEquals("Order must have at least one item.", exception.getMessage());
    }

    @Test
    public void createOrder_ShouldThrowValidationException_WhenNegativeQuantity() {
        // Prepare test data
        OrderRequest request = new OrderRequest();
        OrderRequest.OrderItemRequest itemRequest = new OrderRequest.OrderItemRequest();
        itemRequest.setProductCode("P001");
        itemRequest.setQuantity(-1);
        itemRequest.setUnitPrice(new BigDecimal("10.00"));

        request.setItems(Collections.singletonList(itemRequest));

        // Execute and assert
        OrderValidationException exception = assertThrows(OrderValidationException.class,
                () -> orderService.createOrder(request));
        assertEquals("Quantity must be positive.", exception.getMessage());
    }
}
