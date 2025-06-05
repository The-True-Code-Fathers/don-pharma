package com.codefathers.model.entity;

import java.math.BigDecimal;
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
    
    @ManyToMany
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
