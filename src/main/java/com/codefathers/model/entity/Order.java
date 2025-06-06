package com.codefathers.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "\"order\"")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Employee seller;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @Column(name = "products_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal productsPrice;

    @ManyToOne
    @JoinColumn(name = "shipping_provider_id", nullable = false)
    private ShippingProvider shippingProvider;

    @Column(name = "shipping_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal shippingPrice;

    @Column(name = "total_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalAmount;
    
    @Column(nullable = false)
    private boolean invoiced = false;

    @Column(nullable = false)
    private boolean cancelled = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}
