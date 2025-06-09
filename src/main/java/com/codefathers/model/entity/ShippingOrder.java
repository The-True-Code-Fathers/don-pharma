package com.codefathers.model.entity;

import com.codefathers.model.enums.ShippingServiceStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "shipping_order")
@Data
public class ShippingOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "shipping_provider_id", nullable = false)
    private ShippingProvider shippingProvider;

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


}
