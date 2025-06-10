package com.codefathers.model.entity;

import com.codefathers.model.enums.ShippingServiceStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "shipping_order")
@Data
@Builder
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

    public ShippingOrder() {}
    public ShippingOrder(UUID id,
                         ShippingProvider shippingProvider,
                         String destinationState,
                         String destinationCity,
                         BigDecimal weight,
                         ShippingServiceStatus status,
                         Integer estimatedDeliveryDays,
                         LocalDate shipmentDate,
                         LocalDate deliveryDate,
                         BigDecimal shippingCost) {
        this.id = id;
        this.shippingProvider = shippingProvider;
        this.destinationState = destinationState;
        this.destinationCity = destinationCity;
        this.weight = weight;
        this.status = status;
        this.estimatedDeliveryDays = estimatedDeliveryDays;
        this.shipmentDate = shipmentDate;
        this.deliveryDate = deliveryDate;
        this.shippingCost = shippingCost;
    }

}
