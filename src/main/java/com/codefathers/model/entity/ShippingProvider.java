package com.codefathers.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

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
    private List<ShippingArea> shippingAreas;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Retorna lista de estados distintos atendidos
    public List<String> getServiceStates() {
        if (shippingAreas == null || shippingAreas.isEmpty()) {
            return List.of();
        }
        return shippingAreas.stream()
                .filter(sa -> sa.getStates() != null)
                .flatMap(sa -> sa.getStates().stream())
                .distinct()
                .collect(Collectors.toList());
    }
}
