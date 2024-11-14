package com.example.demo.entity;

import jakarta.persistence.*;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
@Column(nullable = false)
private String orderNumber;
@Column(nullable = false)
@Enumerated(EnumType.STRING)
private OrderStatus status;
@Column(nullable = false)
private LocalDateTime createdAt;
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrderItem> items = new ArrayList<>();
private BigDecimal totalAmount;
}
