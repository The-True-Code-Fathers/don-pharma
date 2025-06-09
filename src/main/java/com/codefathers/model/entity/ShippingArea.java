    package com.codefathers.model.entity;

    import java.util.UUID;
    import java.util.regex.Pattern;

    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    @Data
    @Entity(name = "shipping_area")
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public class ShippingArea {
        @GeneratedValue(strategy = GenerationType.UUID)
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
