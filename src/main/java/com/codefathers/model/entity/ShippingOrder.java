package com.codefathers.model.entity;

import com.codefathers.model.enums.ShippingServiceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "shipping_order")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @NotNull
    private ShippingProvider shippingProvider;

    @OneToMany(mappedBy = "shippingOrder", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "shippingOrder", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Column(name = "purchase_order")
    private List<PurchaseOrder> purchaseOrder = new ArrayList<>();

    @Column(nullable = false)
    private String destinationState;

    @Column(nullable = false)
    private String destinationCity;

    @Column(nullable = false)
    private BigDecimal weight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShippingServiceStatus status;

    @Column(name = "estimated_delivery_days")
    private Integer estimatedDeliveryDays;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "shipment_date")
    private LocalDate shipmentDate;

    @Column(name = "shipping_cost")
    private BigDecimal shippingCost;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
