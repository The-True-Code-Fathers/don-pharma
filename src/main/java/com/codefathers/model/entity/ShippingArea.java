package com.codefathers.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity(name = "shipping_area")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingArea {
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shipping_provider_id")
    private ShippingProvider shippingProvider;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private String[] states;

    @Column(nullable = false)
    private String cep;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
}
