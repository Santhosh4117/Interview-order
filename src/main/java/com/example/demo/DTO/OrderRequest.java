package com.example.demo.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class OrderRequest {

    @NotEmpty
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        @NotEmpty
        private String productCode;

        @Positive
        private Integer quantity;

        @Positive
        private BigDecimal unitPrice;
    }
}
