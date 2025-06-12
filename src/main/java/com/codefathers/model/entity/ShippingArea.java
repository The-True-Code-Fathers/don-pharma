package com.codefathers.model.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity(name = "shipping_area")
@Builder
@Getter
@Setter
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
}
