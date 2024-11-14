package com.example.demo.entity;

import jakarta.persistence.*;

import jdk.jfr.DataAmount;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
@Entity
@Table(name = "order_items")
@Data
public class OrderItem {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "order_id", nullable = false)
private Order order;
@Column(nullable = false)
private String productCode;
@Column(nullable = false)
private Integer quantity;
@Column(nullable = false)
private BigDecimal unitPrice;
}
