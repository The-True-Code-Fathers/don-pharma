package com.codefathers.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity(name = "purchase_order")
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "purchaser_id", nullable = false)
    private Employee purchaserId;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> purchaseItems;

    @Column(name = "purchase_products_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal purchaseProductsPrice;

    @Column(name = "purchase_total_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal purchaseTotalAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


}

