package com.codefathers.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.codefathers.model.enums.PurchaseOrderStatus;

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
    private List<PurchaseOrderItem> purchaseItems = new ArrayList<>();

    @Column(name = "purchase_total_price_amount", nullable = false)
    private BigDecimal purchaseTotalPriceAmount;

    @Column(name = "purchase_total_product_amount", precision = 19, scale = 4, nullable = false)
    private int purchaseTotalProductAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseOrderStatus purchaseOrderStatus;

}

