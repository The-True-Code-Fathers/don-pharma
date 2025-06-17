package com.codefathers.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.codefathers.model.enums.PurchaseOrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<PurchaseOrderItem> purchaseItems;

    @ManyToOne
    @JoinColumn(name = "shipping_order_id", nullable = true)
    private ShippingOrder shippingOrder;

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
