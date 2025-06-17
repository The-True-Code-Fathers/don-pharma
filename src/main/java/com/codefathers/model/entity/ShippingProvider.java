package com.codefathers.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity(name = "shipping_provider")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingProvider {
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
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
    @ToString.Exclude
    @JsonBackReference
    private List<ShippingArea> shippingAreas = new ArrayList<>();
    
    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
