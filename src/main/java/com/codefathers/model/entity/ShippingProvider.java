package com.codefathers.model.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity(name = "shipping_provider")
public class ShippingProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String cnpj;

    @Column(nullable = false)
    private String name;

    @Column(name = "base_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal basePrice;

    @Column(name = "daily_capacity", nullable = false)
    private BigDecimal dailyCapacity;

    @Column(name = "average_delivery_days", nullable = false)
    private int averageDeliveryDays;

    @OneToMany(mappedBy = "shippingProvider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShippingArea> shippingAreas = new ArrayList<>();
}
