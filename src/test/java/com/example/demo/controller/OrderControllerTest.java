package com.example.demo.controller;

import com.example.demo.DTO.OrderRequest;
import com.example.demo.DTO.OrderResponse;
import com.example.demo.entity.OrderStatus;
import com.example.demo.exception.OrderProcessingException;
import com.example.demo.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    public void createOrder_ShouldReturnSuccessResponse() throws Exception {
        // Prepare mock response from OrderService
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setOrderNumber("ORDER12345");
        orderResponse.setStatus(OrderStatus.CREATED);
        orderResponse.setTotalAmount(new BigDecimal("20.00"));
        orderResponse.setCreatedAt(LocalDateTime.now());

        OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse();
        itemResponse.setProductCode("P001");
        itemResponse.setQuantity(2);
        itemResponse.setUnitPrice(new BigDecimal("10.00"));

        orderResponse.setItems(Collections.singletonList(itemResponse));

        // Mock the service call
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(orderResponse);

        // JSON request body
        String requestBody = """
                {
                  "items": [
                    {"productCode": "P001", "quantity": 2, "unitPrice": 10.00}
                  ]
                }
                """;

        // Perform the request and verify response
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORDER12345"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(20.00))
                .andExpect(jsonPath("$.items[0].productCode").value("P001"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(10.00));
    }

    @Test
    public void createOrder_ShouldReturnValidationError_WhenNoItemsProvided() throws Exception {
        // JSON request body with empty items list
        String requestBody = """
                {
                  "items": []
                }
                """;

        // Perform the request and verify response
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").value("Order must have at least one item."));
    }

    @Test
    public void createOrder_ShouldReturnValidationError_WhenQuantityIsNegative() throws Exception {
        // JSON request body with a negative quantity
        String requestBody = """
                {
                  "items": [
                    {"productCode": "P001", "quantity": -2, "unitPrice": 10.00}
                  ]
                }
                """;

        // Perform the request and verify response
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").value("Quantity must be positive."));
    }

    @Test
    public void createOrder_ShouldReturnProcessingError_WhenServiceThrowsException() throws Exception {
        // Prepare mock exception thrown by the service
        when(orderService.createOrder(any(OrderRequest.class)))
                .thenThrow(new OrderProcessingException("Database error"));

        // JSON request body
        String requestBody = """
                {
                  "items": [
                    {"productCode": "P001", "quantity": 2, "unitPrice": 10.00}
                  ]
                }
                """;

        // Perform the request and verify response
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Processing failed"))
                .andExpect(jsonPath("$.details").value("Database error"));
    }
}
