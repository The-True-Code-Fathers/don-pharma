package com.codefathers.model.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JsonBackReference
    private ShippingProvider shippingProvider;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    @ElementCollection // Necessário para mapear arrays ou listas simples
    private List<String> states; // alterado para List<String> para JPA mapear corretamente

    @Column(nullable = false)
    private String cep;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
