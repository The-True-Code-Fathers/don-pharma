    package com.codefathers.model.entity;

    import java.util.UUID;

    import jakarta.persistence.*;
    import lombok.Data;

    @Data
    @Entity(name = "shipping_area")
    public class ShippingArea {
        @Id
        private UUID id;

        @ManyToOne
        @JoinColumn(name = "shipping_provider_id")
        private ShippingProvider shippingProvider;

        @Column(columnDefinition = "text")
        private String description;

        @Column(nullable = false)
        private String states;

    }
