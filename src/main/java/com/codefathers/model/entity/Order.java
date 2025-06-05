package com.codefathers.model.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity(name = "\"order\"")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Employee seller;
    
    @ManyToOne
    @JoinColumn(name = "product_sku", nullable = false)
    private Product product;
    
    @Column(name = "product_quantity", nullable = false)
    private int productQuantity;
    
    @Column(name = "products_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal productsPrice;
    
    @Column(name = "shipping_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal shippingPrice;
    
    @ManyToOne
    @JoinColumn(name = "shipping_provider_id", nullable = false)
    private ShippingProvider shippingProvider;

    @Column(name = "total_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalAmount;
    
    @Column(nullable = false)
    private boolean invoiced;

}
