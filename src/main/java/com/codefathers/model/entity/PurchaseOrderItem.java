package com.codefathers.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity(name = "purchase_order_item")
public class PurchaseOrderItem {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne
    @JoinColumn(name = "purchase_product_sku", nullable = false)
    private Product product;

    @Column(name = "purchase_quantity", nullable = false)
    private int quantity;

    @Column(name = "purchase_price", precision = 19, scale = 4)
    private BigDecimal price;
}

